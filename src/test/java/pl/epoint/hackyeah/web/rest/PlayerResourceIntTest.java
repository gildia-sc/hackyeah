package pl.epoint.hackyeah.web.rest;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import pl.epoint.hackyeah.HackyeahApp;
import pl.epoint.hackyeah.domain.Authority;
import pl.epoint.hackyeah.domain.Player;
import pl.epoint.hackyeah.repository.PlayerRepository;
import pl.epoint.hackyeah.security.AuthoritiesConstants;
import pl.epoint.hackyeah.service.MailService;
import pl.epoint.hackyeah.service.UserService;
import pl.epoint.hackyeah.service.dto.UserDTO;
import pl.epoint.hackyeah.service.mapper.UserMapper;
import pl.epoint.hackyeah.web.rest.errors.ExceptionTranslator;
import pl.epoint.hackyeah.web.rest.vm.ManagedUserVM;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HackyeahApp.class)
public class PlayerResourceIntTest {

    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String UPDATED_LOGIN = "jhipster";

    private static final Long DEFAULT_ID = 1L;

    private static final String DEFAULT_PASSWORD = "passjohndoe";
    private static final String UPDATED_PASSWORD = "passjhipster";

    private static final String DEFAULT_EMAIL = "johndoe@localhost";
    private static final String UPDATED_EMAIL = "jhipster@localhost";

    private static final String DEFAULT_FIRSTNAME = "john";
    private static final String UPDATED_FIRSTNAME = "jhipsterFirstName";

    private static final String DEFAULT_LASTNAME = "doe";
    private static final String UPDATED_LASTNAME = "jhipsterLastName";

    private static final String DEFAULT_IMAGEURL = "http://placehold.it/50x50";
    private static final String UPDATED_IMAGEURL = "http://placehold.it/40x40";

    private static final String DEFAULT_LANGKEY = "en";
    private static final String UPDATED_LANGKEY = "fr";

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restUserMockMvc;

    private Player player;

    @Before
    public void setup() {
        UserResource userResource = new UserResource(userService, playerRepository, mailService);

        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .build();
    }

    /**
     * Create a User.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which has a required relationship to the User entity.
     */
    public static Player createEntity(EntityManager em) {
        Player player = new Player();
        player.setLogin(DEFAULT_LOGIN + RandomStringUtils.randomAlphabetic(5));
        player.setPassword(RandomStringUtils.random(60));
        player.setActivated(true);
        player.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        player.setFirstName(DEFAULT_FIRSTNAME);
        player.setLastName(DEFAULT_LASTNAME);
        player.setImageUrl(DEFAULT_IMAGEURL);
        player.setLangKey(DEFAULT_LANGKEY);
        return player;
    }

    @Before
    public void initTest() {
        player = createEntity(em);
        player.setLogin(DEFAULT_LOGIN);
        player.setEmail(DEFAULT_EMAIL);
    }

    @Test
    @Transactional
    public void createUser() throws Exception {
        int databaseSizeBeforeCreate = playerRepository.findAll().size();

        // Create the User
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(DEFAULT_LOGIN);
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(DEFAULT_IMAGEURL);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isCreated());

        // Validate the User in the database
        List<Player> playerList = playerRepository.findAll();
        assertThat(playerList).hasSize(databaseSizeBeforeCreate + 1);
        Player testPlayer = playerList.get(playerList.size() - 1);
        assertThat(testPlayer.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(testPlayer.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(testPlayer.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(testPlayer.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testPlayer.getImageUrl()).isEqualTo(DEFAULT_IMAGEURL);
        assertThat(testPlayer.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
    }

    @Test
    @Transactional
    public void createUserWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = playerRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(1L);
        managedUserVM.setLogin(DEFAULT_LOGIN);
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(DEFAULT_IMAGEURL);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserMockMvc.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<Player> playerList = playerRepository.findAll();
        assertThat(playerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithExistingLogin() throws Exception {
        // Initialize the database
        playerRepository.saveAndFlush(player);
        int databaseSizeBeforeCreate = playerRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(DEFAULT_LOGIN);// this login should already be used
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail("anothermail@localhost");
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(DEFAULT_IMAGEURL);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Create the User
        restUserMockMvc.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<Player> playerList = playerRepository.findAll();
        assertThat(playerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithExistingEmail() throws Exception {
        // Initialize the database
        playerRepository.saveAndFlush(player);
        int databaseSizeBeforeCreate = playerRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("anotherlogin");
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);// this email should already be used
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(DEFAULT_IMAGEURL);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Create the User
        restUserMockMvc.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<Player> playerList = playerRepository.findAll();
        assertThat(playerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllUsers() throws Exception {
        // Initialize the database
        playerRepository.saveAndFlush(player);

        // Get all the users
        restUserMockMvc.perform(get("/api/users?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN)))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRSTNAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LASTNAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].imageUrl").value(hasItem(DEFAULT_IMAGEURL)))
            .andExpect(jsonPath("$.[*].langKey").value(hasItem(DEFAULT_LANGKEY)));
    }

    @Test
    @Transactional
    public void getUser() throws Exception {
        // Initialize the database
        playerRepository.saveAndFlush(player);

        // Get the user
        restUserMockMvc.perform(get("/api/users/{login}", player.getLogin()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.login").value(player.getLogin()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRSTNAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LASTNAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.imageUrl").value(DEFAULT_IMAGEURL))
            .andExpect(jsonPath("$.langKey").value(DEFAULT_LANGKEY));
    }

    @Test
    @Transactional
    public void getNonExistingUser() throws Exception {
        restUserMockMvc.perform(get("/api/users/unknown"))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateUser() throws Exception {
        // Initialize the database
        playerRepository.saveAndFlush(player);
        int databaseSizeBeforeUpdate = playerRepository.findAll().size();

        // Update the user
        Player updatedPlayer = playerRepository.findById(player.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedPlayer.getId());
        managedUserVM.setLogin(updatedPlayer.getLogin());
        managedUserVM.setPassword(UPDATED_PASSWORD);
        managedUserVM.setFirstName(UPDATED_FIRSTNAME);
        managedUserVM.setLastName(UPDATED_LASTNAME);
        managedUserVM.setEmail(UPDATED_EMAIL);
        managedUserVM.setActivated(updatedPlayer.getActivated());
        managedUserVM.setImageUrl(UPDATED_IMAGEURL);
        managedUserVM.setLangKey(UPDATED_LANGKEY);
        managedUserVM.setCreatedBy(updatedPlayer.getCreatedBy());
        managedUserVM.setCreatedDate(updatedPlayer.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedPlayer.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedPlayer.getLastModifiedDate());
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc.perform(put("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isOk());

        // Validate the User in the database
        List<Player> playerList = playerRepository.findAll();
        assertThat(playerList).hasSize(databaseSizeBeforeUpdate);
        Player testPlayer = playerList.get(playerList.size() - 1);
        assertThat(testPlayer.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
        assertThat(testPlayer.getLastName()).isEqualTo(UPDATED_LASTNAME);
        assertThat(testPlayer.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testPlayer.getImageUrl()).isEqualTo(UPDATED_IMAGEURL);
        assertThat(testPlayer.getLangKey()).isEqualTo(UPDATED_LANGKEY);
    }

    @Test
    @Transactional
    public void updateUserLogin() throws Exception {
        // Initialize the database
        playerRepository.saveAndFlush(player);
        int databaseSizeBeforeUpdate = playerRepository.findAll().size();

        // Update the user
        Player updatedPlayer = playerRepository.findById(player.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedPlayer.getId());
        managedUserVM.setLogin(UPDATED_LOGIN);
        managedUserVM.setPassword(UPDATED_PASSWORD);
        managedUserVM.setFirstName(UPDATED_FIRSTNAME);
        managedUserVM.setLastName(UPDATED_LASTNAME);
        managedUserVM.setEmail(UPDATED_EMAIL);
        managedUserVM.setActivated(updatedPlayer.getActivated());
        managedUserVM.setImageUrl(UPDATED_IMAGEURL);
        managedUserVM.setLangKey(UPDATED_LANGKEY);
        managedUserVM.setCreatedBy(updatedPlayer.getCreatedBy());
        managedUserVM.setCreatedDate(updatedPlayer.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedPlayer.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedPlayer.getLastModifiedDate());
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc.perform(put("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isOk());

        // Validate the User in the database
        List<Player> playerList = playerRepository.findAll();
        assertThat(playerList).hasSize(databaseSizeBeforeUpdate);
        Player testPlayer = playerList.get(playerList.size() - 1);
        assertThat(testPlayer.getLogin()).isEqualTo(UPDATED_LOGIN);
        assertThat(testPlayer.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
        assertThat(testPlayer.getLastName()).isEqualTo(UPDATED_LASTNAME);
        assertThat(testPlayer.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testPlayer.getImageUrl()).isEqualTo(UPDATED_IMAGEURL);
        assertThat(testPlayer.getLangKey()).isEqualTo(UPDATED_LANGKEY);
    }

    @Test
    @Transactional
    public void updateUserExistingEmail() throws Exception {
        // Initialize the database with 2 users
        playerRepository.saveAndFlush(player);

        Player anotherPlayer = new Player();
        anotherPlayer.setLogin("jhipster");
        anotherPlayer.setPassword(RandomStringUtils.random(60));
        anotherPlayer.setActivated(true);
        anotherPlayer.setEmail("jhipster@localhost");
        anotherPlayer.setFirstName("java");
        anotherPlayer.setLastName("hipster");
        anotherPlayer.setImageUrl("");
        anotherPlayer.setLangKey("en");
        playerRepository.saveAndFlush(anotherPlayer);

        // Update the user
        Player updatedPlayer = playerRepository.findById(player.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedPlayer.getId());
        managedUserVM.setLogin(updatedPlayer.getLogin());
        managedUserVM.setPassword(updatedPlayer.getPassword());
        managedUserVM.setFirstName(updatedPlayer.getFirstName());
        managedUserVM.setLastName(updatedPlayer.getLastName());
        managedUserVM.setEmail("jhipster@localhost");// this email should already be used by anotherUser
        managedUserVM.setActivated(updatedPlayer.getActivated());
        managedUserVM.setImageUrl(updatedPlayer.getImageUrl());
        managedUserVM.setLangKey(updatedPlayer.getLangKey());
        managedUserVM.setCreatedBy(updatedPlayer.getCreatedBy());
        managedUserVM.setCreatedDate(updatedPlayer.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedPlayer.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedPlayer.getLastModifiedDate());
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc.perform(put("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateUserExistingLogin() throws Exception {
        // Initialize the database
        playerRepository.saveAndFlush(player);

        Player anotherPlayer = new Player();
        anotherPlayer.setLogin("jhipster");
        anotherPlayer.setPassword(RandomStringUtils.random(60));
        anotherPlayer.setActivated(true);
        anotherPlayer.setEmail("jhipster@localhost");
        anotherPlayer.setFirstName("java");
        anotherPlayer.setLastName("hipster");
        anotherPlayer.setImageUrl("");
        anotherPlayer.setLangKey("en");
        playerRepository.saveAndFlush(anotherPlayer);

        // Update the user
        Player updatedPlayer = playerRepository.findById(player.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedPlayer.getId());
        managedUserVM.setLogin("jhipster");// this login should already be used by anotherUser
        managedUserVM.setPassword(updatedPlayer.getPassword());
        managedUserVM.setFirstName(updatedPlayer.getFirstName());
        managedUserVM.setLastName(updatedPlayer.getLastName());
        managedUserVM.setEmail(updatedPlayer.getEmail());
        managedUserVM.setActivated(updatedPlayer.getActivated());
        managedUserVM.setImageUrl(updatedPlayer.getImageUrl());
        managedUserVM.setLangKey(updatedPlayer.getLangKey());
        managedUserVM.setCreatedBy(updatedPlayer.getCreatedBy());
        managedUserVM.setCreatedDate(updatedPlayer.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedPlayer.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedPlayer.getLastModifiedDate());
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc.perform(put("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void deleteUser() throws Exception {
        // Initialize the database
        playerRepository.saveAndFlush(player);
        int databaseSizeBeforeDelete = playerRepository.findAll().size();

        // Delete the user
        restUserMockMvc.perform(delete("/api/users/{login}", player.getLogin())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Player> playerList = playerRepository.findAll();
        assertThat(playerList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void getAllAuthorities() throws Exception {
        restUserMockMvc.perform(get("/api/users/authorities")
            .accept(TestUtil.APPLICATION_JSON_UTF8)
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").value(hasItems(AuthoritiesConstants.USER, AuthoritiesConstants.ADMIN)));
    }

    @Test
    @Transactional
    public void testUserEquals() throws Exception {
        TestUtil.equalsVerifier(Player.class);
        Player player1 = new Player();
        player1.setId(1L);
        Player player2 = new Player();
        player2.setId(player1.getId());
        assertThat(player1).isEqualTo(player2);
        player2.setId(2L);
        assertThat(player1).isNotEqualTo(player2);
        player1.setId(null);
        assertThat(player1).isNotEqualTo(player2);
    }

    @Test
    public void testUserFromId() {
        assertThat(userMapper.userFromId(DEFAULT_ID).getId()).isEqualTo(DEFAULT_ID);
        assertThat(userMapper.userFromId(null)).isNull();
    }

    @Test
    public void testUserDTOtoUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(DEFAULT_ID);
        userDTO.setLogin(DEFAULT_LOGIN);
        userDTO.setFirstName(DEFAULT_FIRSTNAME);
        userDTO.setLastName(DEFAULT_LASTNAME);
        userDTO.setEmail(DEFAULT_EMAIL);
        userDTO.setActivated(true);
        userDTO.setImageUrl(DEFAULT_IMAGEURL);
        userDTO.setLangKey(DEFAULT_LANGKEY);
        userDTO.setCreatedBy(DEFAULT_LOGIN);
        userDTO.setLastModifiedBy(DEFAULT_LOGIN);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        Player player = userMapper.userDTOToUser(userDTO);
        assertThat(player.getId()).isEqualTo(DEFAULT_ID);
        assertThat(player.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(player.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(player.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(player.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(player.getActivated()).isEqualTo(true);
        assertThat(player.getImageUrl()).isEqualTo(DEFAULT_IMAGEURL);
        assertThat(player.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(player.getCreatedBy()).isNull();
        assertThat(player.getCreatedDate()).isNotNull();
        assertThat(player.getLastModifiedBy()).isNull();
        assertThat(player.getLastModifiedDate()).isNotNull();
        assertThat(player.getAuthorities()).extracting("name").containsExactly(AuthoritiesConstants.USER);
    }

    @Test
    public void testUserToUserDTO() {
        player.setId(DEFAULT_ID);
        player.setCreatedBy(DEFAULT_LOGIN);
        player.setCreatedDate(Instant.now());
        player.setLastModifiedBy(DEFAULT_LOGIN);
        player.setLastModifiedDate(Instant.now());
        Set<Authority> authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.USER);
        authorities.add(authority);
        player.setAuthorities(authorities);

        UserDTO userDTO = userMapper.userToUserDTO(player);

        assertThat(userDTO.getId()).isEqualTo(DEFAULT_ID);
        assertThat(userDTO.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(userDTO.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(userDTO.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(userDTO.isActivated()).isEqualTo(true);
        assertThat(userDTO.getImageUrl()).isEqualTo(DEFAULT_IMAGEURL);
        assertThat(userDTO.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(userDTO.getCreatedBy()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getCreatedDate()).isEqualTo(player.getCreatedDate());
        assertThat(userDTO.getLastModifiedBy()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getLastModifiedDate()).isEqualTo(player.getLastModifiedDate());
        assertThat(userDTO.getAuthorities()).containsExactly(AuthoritiesConstants.USER);
        assertThat(userDTO.toString()).isNotNull();
    }

    @Test
    public void testAuthorityEquals() {
        Authority authorityA = new Authority();
        assertThat(authorityA).isEqualTo(authorityA);
        assertThat(authorityA).isNotEqualTo(null);
        assertThat(authorityA).isNotEqualTo(new Object());
        assertThat(authorityA.hashCode()).isEqualTo(0);
        assertThat(authorityA.toString()).isNotNull();

        Authority authorityB = new Authority();
        assertThat(authorityA).isEqualTo(authorityB);

        authorityB.setName(AuthoritiesConstants.ADMIN);
        assertThat(authorityA).isNotEqualTo(authorityB);

        authorityA.setName(AuthoritiesConstants.USER);
        assertThat(authorityA).isNotEqualTo(authorityB);

        authorityB.setName(AuthoritiesConstants.USER);
        assertThat(authorityA).isEqualTo(authorityB);
        assertThat(authorityA.hashCode()).isEqualTo(authorityB.hashCode());
    }
}
