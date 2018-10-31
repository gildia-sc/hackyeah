package pl.epoint.hackyeah.web.rest;

import pl.epoint.hackyeah.HackyeahApp;
import pl.epoint.hackyeah.config.Constants;
import pl.epoint.hackyeah.domain.Authority;
import pl.epoint.hackyeah.domain.PersistentToken;
import pl.epoint.hackyeah.domain.Player;
import pl.epoint.hackyeah.repository.AuthorityRepository;
import pl.epoint.hackyeah.repository.PersistentTokenRepository;
import pl.epoint.hackyeah.repository.PlayerRepository;
import pl.epoint.hackyeah.security.AuthoritiesConstants;
import pl.epoint.hackyeah.service.MailService;
import pl.epoint.hackyeah.service.UserService;
import pl.epoint.hackyeah.service.dto.PasswordChangeDTO;
import pl.epoint.hackyeah.service.dto.UserDTO;
import pl.epoint.hackyeah.web.rest.errors.ExceptionTranslator;
import pl.epoint.hackyeah.web.rest.vm.KeyAndPasswordVM;
import pl.epoint.hackyeah.web.rest.vm.ManagedUserVM;
import org.apache.commons.lang3.RandomStringUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the AccountResource REST controller.
 *
 * @see AccountResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HackyeahApp.class)
public class AccountResourceIntTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PersistentTokenRepository persistentTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpMessageConverter<?>[] httpMessageConverters;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Mock
    private UserService mockUserService;

    @Mock
    private MailService mockMailService;

    private MockMvc restMvc;

    private MockMvc restUserMockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mockMailService).sendActivationEmail(any());
        AccountResource accountResource =
            new AccountResource(playerRepository, userService, mockMailService, persistentTokenRepository);

        AccountResource accountUserMockResource =
            new AccountResource(playerRepository, mockUserService, mockMailService, persistentTokenRepository);
        this.restMvc = MockMvcBuilders.standaloneSetup(accountResource)
            .setMessageConverters(httpMessageConverters)
            .setControllerAdvice(exceptionTranslator)
            .build();
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource)
            .setControllerAdvice(exceptionTranslator)
            .build();
    }

    @Test
    public void testNonAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/authenticate")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    @Test
    public void testAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/authenticate")
            .with(request -> {
                request.setRemoteUser("test");
                return request;
            })
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("test"));
    }

    @Test
    public void testGetExistingAccount() throws Exception {
        Set<Authority> authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.ADMIN);
        authorities.add(authority);

        Player player = new Player();
        player.setLogin("test");
        player.setFirstName("john");
        player.setLastName("doe");
        player.setEmail("john.doe@jhipster.com");
        player.setImageUrl("http://placehold.it/50x50");
        player.setLangKey("en");
        player.setAuthorities(authorities);
        when(mockUserService.getPlayerWithAuthorities()).thenReturn(Optional.of(player));

        restUserMockMvc.perform(get("/api/account")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.login").value("test"))
            .andExpect(jsonPath("$.firstName").value("john"))
            .andExpect(jsonPath("$.lastName").value("doe"))
            .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
            .andExpect(jsonPath("$.imageUrl").value("http://placehold.it/50x50"))
            .andExpect(jsonPath("$.langKey").value("en"))
            .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN));
    }

    @Test
    public void testGetUnknownAccount() throws Exception {
        when(mockUserService.getPlayerWithAuthorities()).thenReturn(Optional.empty());

        restUserMockMvc.perform(get("/api/account")
            .accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    public void testRegisterValid() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setLogin("test-register-valid");
        validUser.setPassword("password");
        validUser.setFirstName("Alice");
        validUser.setLastName("Test");
        validUser.setEmail("test-register-valid@example.com");
        validUser.setImageUrl("http://placehold.it/50x50");
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        assertThat(playerRepository.findOneByLogin("test-register-valid").isPresent()).isFalse();

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        assertThat(playerRepository.findOneByLogin("test-register-valid").isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testRegisterInvalidLogin() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("funky-log!n");// <-- invalid
        invalidUser.setPassword("password");
        invalidUser.setFirstName("Funky");
        invalidUser.setLastName("One");
        invalidUser.setEmail("funky@example.com");
        invalidUser.setActivated(true);
        invalidUser.setImageUrl("http://placehold.it/50x50");
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<Player> user = playerRepository.findOneByEmailIgnoreCase("funky@example.com");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterInvalidEmail() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("bob");
        invalidUser.setPassword("password");
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setEmail("invalid");// <-- invalid
        invalidUser.setActivated(true);
        invalidUser.setImageUrl("http://placehold.it/50x50");
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<Player> user = playerRepository.findOneByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterInvalidPassword() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("bob");
        invalidUser.setPassword("123");// password with only 3 digits
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setEmail("bob@example.com");
        invalidUser.setActivated(true);
        invalidUser.setImageUrl("http://placehold.it/50x50");
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<Player> user = playerRepository.findOneByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterNullPassword() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("bob");
        invalidUser.setPassword(null);// invalid null password
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setEmail("bob@example.com");
        invalidUser.setActivated(true);
        invalidUser.setImageUrl("http://placehold.it/50x50");
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<Player> user = playerRepository.findOneByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterDuplicateLogin() throws Exception {
        // First registration
        ManagedUserVM firstUser = new ManagedUserVM();
        firstUser.setLogin("alice");
        firstUser.setPassword("password");
        firstUser.setFirstName("Alice");
        firstUser.setLastName("Something");
        firstUser.setEmail("alice@example.com");
        firstUser.setImageUrl("http://placehold.it/50x50");
        firstUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        firstUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Duplicate login, different email
        ManagedUserVM secondUser = new ManagedUserVM();
        secondUser.setLogin(firstUser.getLogin());
        secondUser.setPassword(firstUser.getPassword());
        secondUser.setFirstName(firstUser.getFirstName());
        secondUser.setLastName(firstUser.getLastName());
        secondUser.setEmail("alice2@example.com");
        secondUser.setImageUrl(firstUser.getImageUrl());
        secondUser.setLangKey(firstUser.getLangKey());
        secondUser.setCreatedBy(firstUser.getCreatedBy());
        secondUser.setCreatedDate(firstUser.getCreatedDate());
        secondUser.setLastModifiedBy(firstUser.getLastModifiedBy());
        secondUser.setLastModifiedDate(firstUser.getLastModifiedDate());
        secondUser.setAuthorities(new HashSet<>(firstUser.getAuthorities()));

        // First user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(firstUser)))
            .andExpect(status().isCreated());

        // Second (non activated) user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().isCreated());

        Optional<Player> testUser = playerRepository.findOneByEmailIgnoreCase("alice2@example.com");
        assertThat(testUser.isPresent()).isTrue();
        testUser.get().setActivated(true);
        playerRepository.save(testUser.get());

        // Second (already activated) user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    public void testRegisterDuplicateEmail() throws Exception {
        // First user
        ManagedUserVM firstUser = new ManagedUserVM();
        firstUser.setLogin("test-register-duplicate-email");
        firstUser.setPassword("password");
        firstUser.setFirstName("Alice");
        firstUser.setLastName("Test");
        firstUser.setEmail("test-register-duplicate-email@example.com");
        firstUser.setImageUrl("http://placehold.it/50x50");
        firstUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        firstUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Register first user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(firstUser)))
            .andExpect(status().isCreated());

        Optional<Player> testUser1 = playerRepository.findOneByLogin("test-register-duplicate-email");
        assertThat(testUser1.isPresent()).isTrue();

        // Duplicate email, different login
        ManagedUserVM secondUser = new ManagedUserVM();
        secondUser.setLogin("test-register-duplicate-email-2");
        secondUser.setPassword(firstUser.getPassword());
        secondUser.setFirstName(firstUser.getFirstName());
        secondUser.setLastName(firstUser.getLastName());
        secondUser.setEmail(firstUser.getEmail());
        secondUser.setImageUrl(firstUser.getImageUrl());
        secondUser.setLangKey(firstUser.getLangKey());
        secondUser.setAuthorities(new HashSet<>(firstUser.getAuthorities()));

        // Register second (non activated) user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().isCreated());

        Optional<Player> testUser2 = playerRepository.findOneByLogin("test-register-duplicate-email");
        assertThat(testUser2.isPresent()).isFalse();

        Optional<Player> testUser3 = playerRepository.findOneByLogin("test-register-duplicate-email-2");
        assertThat(testUser3.isPresent()).isTrue();

        // Duplicate email - with uppercase email address
        ManagedUserVM userWithUpperCaseEmail = new ManagedUserVM();
        userWithUpperCaseEmail.setId(firstUser.getId());
        userWithUpperCaseEmail.setLogin("test-register-duplicate-email-3");
        userWithUpperCaseEmail.setPassword(firstUser.getPassword());
        userWithUpperCaseEmail.setFirstName(firstUser.getFirstName());
        userWithUpperCaseEmail.setLastName(firstUser.getLastName());
        userWithUpperCaseEmail.setEmail("TEST-register-duplicate-email@example.com");
        userWithUpperCaseEmail.setImageUrl(firstUser.getImageUrl());
        userWithUpperCaseEmail.setLangKey(firstUser.getLangKey());
        userWithUpperCaseEmail.setAuthorities(new HashSet<>(firstUser.getAuthorities()));

        // Register third (not activated) user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userWithUpperCaseEmail)))
            .andExpect(status().isCreated());

        Optional<Player> testUser4 = playerRepository.findOneByLogin("test-register-duplicate-email-3");
        assertThat(testUser4.isPresent()).isTrue();
        assertThat(testUser4.get().getEmail()).isEqualTo("test-register-duplicate-email@example.com");

        testUser4.get().setActivated(true);
        userService.updateUser((new UserDTO(testUser4.get())));

        // Register 4th (already activated) user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    public void testRegisterAdminIsIgnored() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setLogin("badguy");
        validUser.setPassword("password");
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setEmail("badguy@example.com");
        validUser.setActivated(true);
        validUser.setImageUrl("http://placehold.it/50x50");
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<Player> userDup = playerRepository.findOneByLogin("badguy");
        assertThat(userDup.isPresent()).isTrue();
        assertThat(userDup.get().getAuthorities()).hasSize(1)
            .containsExactly(authorityRepository.findById(AuthoritiesConstants.USER).get());
    }

    @Test
    @Transactional
    public void testActivateAccount() throws Exception {
        final String activationKey = "some activation key";
        Player player = new Player();
        player.setLogin("activate-account");
        player.setEmail("activate-account@example.com");
        player.setPassword(RandomStringUtils.random(60));
        player.setActivated(false);
        player.setActivationKey(activationKey);

        playerRepository.saveAndFlush(player);

        restMvc.perform(get("/api/activate?key={activationKey}", activationKey))
            .andExpect(status().isOk());

        player = playerRepository.findOneByLogin(player.getLogin()).orElse(null);
        assertThat(player.getActivated()).isTrue();
    }

    @Test
    @Transactional
    public void testActivateAccountWithWrongKey() throws Exception {
        restMvc.perform(get("/api/activate?key=wrongActivationKey"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    @WithMockUser("save-account")
    public void testSaveAccount() throws Exception {
        Player player = new Player();
        player.setLogin("save-account");
        player.setEmail("save-account@example.com");
        player.setPassword(RandomStringUtils.random(60));
        player.setActivated(true);

        playerRepository.saveAndFlush(player);

        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-account@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        restMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
            .andExpect(status().isOk());

        Player updatedPlayer = playerRepository.findOneByLogin(player.getLogin()).orElse(null);
        assertThat(updatedPlayer.getFirstName()).isEqualTo(userDTO.getFirstName());
        assertThat(updatedPlayer.getLastName()).isEqualTo(userDTO.getLastName());
        assertThat(updatedPlayer.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(updatedPlayer.getLangKey()).isEqualTo(userDTO.getLangKey());
        assertThat(updatedPlayer.getPassword()).isEqualTo(player.getPassword());
        assertThat(updatedPlayer.getImageUrl()).isEqualTo(userDTO.getImageUrl());
        assertThat(updatedPlayer.getActivated()).isEqualTo(true);
        assertThat(updatedPlayer.getAuthorities()).isEmpty();
    }

    @Test
    @Transactional
    @WithMockUser("save-invalid-email")
    public void testSaveInvalidEmail() throws Exception {
        Player player = new Player();
        player.setLogin("save-invalid-email");
        player.setEmail("save-invalid-email@example.com");
        player.setPassword(RandomStringUtils.random(60));
        player.setActivated(true);

        playerRepository.saveAndFlush(player);

        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("invalid email");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        restMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
            .andExpect(status().isBadRequest());

        assertThat(playerRepository.findOneByEmailIgnoreCase("invalid email")).isNotPresent();
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email")
    public void testSaveExistingEmail() throws Exception {
        Player player = new Player();
        player.setLogin("save-existing-email");
        player.setEmail("save-existing-email@example.com");
        player.setPassword(RandomStringUtils.random(60));
        player.setActivated(true);

        playerRepository.saveAndFlush(player);

        Player anotherPlayer = new Player();
        anotherPlayer.setLogin("save-existing-email2");
        anotherPlayer.setEmail("save-existing-email2@example.com");
        anotherPlayer.setPassword(RandomStringUtils.random(60));
        anotherPlayer.setActivated(true);

        playerRepository.saveAndFlush(anotherPlayer);

        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-existing-email2@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        restMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
            .andExpect(status().isBadRequest());

        Player updatedPlayer = playerRepository.findOneByLogin("save-existing-email").orElse(null);
        assertThat(updatedPlayer.getEmail()).isEqualTo("save-existing-email@example.com");
    }

    @Test
    @Transactional
    @WithMockUser("save-existing-email-and-login")
    public void testSaveExistingEmailAndLogin() throws Exception {
        Player player = new Player();
        player.setLogin("save-existing-email-and-login");
        player.setEmail("save-existing-email-and-login@example.com");
        player.setPassword(RandomStringUtils.random(60));
        player.setActivated(true);

        playerRepository.saveAndFlush(player);

        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-existing-email-and-login@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        restMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO)))
            .andExpect(status().isOk());

        Player updatedPlayer = playerRepository.findOneByLogin("save-existing-email-and-login").orElse(null);
        assertThat(updatedPlayer.getEmail()).isEqualTo("save-existing-email-and-login@example.com");
    }

    @Test
    @Transactional
    @WithMockUser("change-password-wrong-existing-password")
    public void testChangePasswordWrongExistingPassword() throws Exception {
        Player player = new Player();
        String currentPassword = RandomStringUtils.random(60);
        player.setPassword(passwordEncoder.encode(currentPassword));
        player.setLogin("change-password-wrong-existing-password");
        player.setEmail("change-password-wrong-existing-password@example.com");
        playerRepository.saveAndFlush(player);

        restMvc.perform(post("/api/account/change-password")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO("1"+currentPassword, "new password"))))
            .andExpect(status().isBadRequest());

        Player updatedPlayer = playerRepository.findOneByLogin("change-password-wrong-existing-password").orElse(null);
        assertThat(passwordEncoder.matches("new password", updatedPlayer.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(currentPassword, updatedPlayer.getPassword())).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser("change-password")
    public void testChangePassword() throws Exception {
        Player player = new Player();
        String currentPassword = RandomStringUtils.random(60);
        player.setPassword(passwordEncoder.encode(currentPassword));
        player.setLogin("change-password");
        player.setEmail("change-password@example.com");
        playerRepository.saveAndFlush(player);

        restMvc.perform(post("/api/account/change-password")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, "new password"))))
            .andExpect(status().isOk());

        Player updatedPlayer = playerRepository.findOneByLogin("change-password").orElse(null);
        assertThat(passwordEncoder.matches("new password", updatedPlayer.getPassword())).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-small")
    public void testChangePasswordTooSmall() throws Exception {
        Player player = new Player();
        String currentPassword = RandomStringUtils.random(60);
        player.setPassword(passwordEncoder.encode(currentPassword));
        player.setLogin("change-password-too-small");
        player.setEmail("change-password-too-small@example.com");
        playerRepository.saveAndFlush(player);

        restMvc.perform(post("/api/account/change-password")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, "new"))))
            .andExpect(status().isBadRequest());

        Player updatedPlayer = playerRepository.findOneByLogin("change-password-too-small").orElse(null);
        assertThat(updatedPlayer.getPassword()).isEqualTo(player.getPassword());
    }

    @Test
    @Transactional
    @WithMockUser("change-password-too-long")
    public void testChangePasswordTooLong() throws Exception {
        Player player = new Player();
        String currentPassword = RandomStringUtils.random(60);
        player.setPassword(passwordEncoder.encode(currentPassword));
        player.setLogin("change-password-too-long");
        player.setEmail("change-password-too-long@example.com");
        playerRepository.saveAndFlush(player);

        restMvc.perform(post("/api/account/change-password")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, RandomStringUtils.random(101)))))
            .andExpect(status().isBadRequest());

        Player updatedPlayer = playerRepository.findOneByLogin("change-password-too-long").orElse(null);
        assertThat(updatedPlayer.getPassword()).isEqualTo(player.getPassword());
    }

    @Test
    @Transactional
    @WithMockUser("change-password-empty")
    public void testChangePasswordEmpty() throws Exception {
        Player player = new Player();
        player.setPassword(RandomStringUtils.random(60));
        player.setLogin("change-password-empty");
        player.setEmail("change-password-empty@example.com");
        playerRepository.saveAndFlush(player);

        restMvc.perform(post("/api/account/change-password").content(RandomStringUtils.random(0)))
            .andExpect(status().isBadRequest());

        Player updatedPlayer = playerRepository.findOneByLogin("change-password-empty").orElse(null);
        assertThat(updatedPlayer.getPassword()).isEqualTo(player.getPassword());
    }

    @Test
    @Transactional
    @WithMockUser("current-sessions")
    public void testGetCurrentSessions() throws Exception {
        Player player = new Player();
        player.setPassword(RandomStringUtils.random(60));
        player.setLogin("current-sessions");
        player.setEmail("current-sessions@example.com");
        playerRepository.saveAndFlush(player);

        PersistentToken token = new PersistentToken();
        token.setSeries("current-sessions");
        token.setPlayer(player);
        token.setTokenValue("current-session-data");
        token.setTokenDate(LocalDate.of(2017, 3, 23));
        token.setIpAddress("127.0.0.1");
        token.setUserAgent("Test agent");
        persistentTokenRepository.saveAndFlush(token);

        restMvc.perform(get("/api/account/sessions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.[*].series").value(hasItem(token.getSeries())))
            .andExpect(jsonPath("$.[*].ipAddress").value(hasItem(token.getIpAddress())))
            .andExpect(jsonPath("$.[*].userAgent").value(hasItem(token.getUserAgent())))
            .andExpect(jsonPath("$.[*].tokenDate").value(hasItem(token.getTokenDate().toString())));
    }

    @Test
    @Transactional
    @WithMockUser("invalidate-session")
    public void testInvalidateSession() throws Exception {
        Player player = new Player();
        player.setPassword(RandomStringUtils.random(60));
        player.setLogin("invalidate-session");
        player.setEmail("invalidate-session@example.com");
        playerRepository.saveAndFlush(player);

        PersistentToken token = new PersistentToken();
        token.setSeries("invalidate-session");
        token.setPlayer(player);
        token.setTokenValue("invalidate-data");
        token.setTokenDate(LocalDate.of(2017, 3, 23));
        token.setIpAddress("127.0.0.1");
        token.setUserAgent("Test agent");
        persistentTokenRepository.saveAndFlush(token);

        assertThat(persistentTokenRepository.findByPlayer(player)).hasSize(1);

        restMvc.perform(delete("/api/account/sessions/invalidate-session"))
            .andExpect(status().isOk());

        assertThat(persistentTokenRepository.findByPlayer(player)).isEmpty();
    }

    @Test
    @Transactional
    public void testRequestPasswordReset() throws Exception {
        Player player = new Player();
        player.setPassword(RandomStringUtils.random(60));
        player.setActivated(true);
        player.setLogin("password-reset");
        player.setEmail("password-reset@example.com");
        playerRepository.saveAndFlush(player);

        restMvc.perform(post("/api/account/reset-password/init")
            .content("password-reset@example.com"))
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void testRequestPasswordResetUpperCaseEmail() throws Exception {
        Player player = new Player();
        player.setPassword(RandomStringUtils.random(60));
        player.setActivated(true);
        player.setLogin("password-reset");
        player.setEmail("password-reset@example.com");
        playerRepository.saveAndFlush(player);

        restMvc.perform(post("/api/account/reset-password/init")
            .content("password-reset@EXAMPLE.COM"))
            .andExpect(status().isOk());
    }

    @Test
    public void testRequestPasswordResetWrongEmail() throws Exception {
        restMvc.perform(
            post("/api/account/reset-password/init")
                .content("password-reset-wrong-email@example.com"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void testFinishPasswordReset() throws Exception {
        Player player = new Player();
        player.setPassword(RandomStringUtils.random(60));
        player.setLogin("finish-password-reset");
        player.setEmail("finish-password-reset@example.com");
        player.setResetDate(Instant.now().plusSeconds(60));
        player.setResetKey("reset key");
        playerRepository.saveAndFlush(player);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(player.getResetKey());
        keyAndPassword.setNewPassword("new password");

        restMvc.perform(
            post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
            .andExpect(status().isOk());

        Player updatedPlayer = playerRepository.findOneByLogin(player.getLogin()).orElse(null);
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedPlayer.getPassword())).isTrue();
    }

    @Test
    @Transactional
    public void testFinishPasswordResetTooSmall() throws Exception {
        Player player = new Player();
        player.setPassword(RandomStringUtils.random(60));
        player.setLogin("finish-password-reset-too-small");
        player.setEmail("finish-password-reset-too-small@example.com");
        player.setResetDate(Instant.now().plusSeconds(60));
        player.setResetKey("reset key too small");
        playerRepository.saveAndFlush(player);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(player.getResetKey());
        keyAndPassword.setNewPassword("foo");

        restMvc.perform(
            post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
            .andExpect(status().isBadRequest());

        Player updatedPlayer = playerRepository.findOneByLogin(player.getLogin()).orElse(null);
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedPlayer.getPassword())).isFalse();
    }


    @Test
    @Transactional
    public void testFinishPasswordResetWrongKey() throws Exception {
        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey("wrong reset key");
        keyAndPassword.setNewPassword("new password");

        restMvc.perform(
            post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
            .andExpect(status().isInternalServerError());
    }
}
