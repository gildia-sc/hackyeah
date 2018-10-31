package pl.epoint.hackyeah.service;

import pl.epoint.hackyeah.HackyeahApp;
import pl.epoint.hackyeah.config.Constants;
import pl.epoint.hackyeah.domain.PersistentToken;
import pl.epoint.hackyeah.domain.Player;
import pl.epoint.hackyeah.repository.PersistentTokenRepository;
import pl.epoint.hackyeah.repository.search.UserSearchRepository;
import pl.epoint.hackyeah.repository.PlayerRepository;
import pl.epoint.hackyeah.service.dto.UserDTO;
import pl.epoint.hackyeah.service.util.RandomUtil;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HackyeahApp.class)
@Transactional
public class PlayerServiceIntTest {

    @Autowired
    private PersistentTokenRepository persistentTokenRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private UserService userService;

    /**
     * This repository is mocked in the pl.epoint.hackyeah.repository.search test package.
     *
     * @see pl.epoint.hackyeah.repository.search.UserSearchRepositoryMockConfiguration
     */
    @Autowired
    private UserSearchRepository mockUserSearchRepository;

    @Autowired
    private AuditingHandler auditingHandler;

    @Mock
    DateTimeProvider dateTimeProvider;

    private Player player;

    @Before
    public void init() {
        persistentTokenRepository.deleteAll();
        player = new Player();
        player.setLogin("johndoe");
        player.setPassword(RandomStringUtils.random(60));
        player.setActivated(true);
        player.setEmail("johndoe@localhost");
        player.setFirstName("john");
        player.setLastName("doe");
        player.setImageUrl("http://placehold.it/50x50");
        player.setLangKey("en");

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.now()));
        auditingHandler.setDateTimeProvider(dateTimeProvider);
    }

    @Test
    @Transactional
    public void testRemoveOldPersistentTokens() {
        playerRepository.saveAndFlush(player);
        int existingCount = persistentTokenRepository.findByPlayer(player).size();
        LocalDate today = LocalDate.now();
        generateUserToken(player, "1111-1111", today);
        generateUserToken(player, "2222-2222", today.minusDays(32));
        assertThat(persistentTokenRepository.findByPlayer(player)).hasSize(existingCount + 2);
        userService.removeOldPersistentTokens();
        assertThat(persistentTokenRepository.findByPlayer(player)).hasSize(existingCount + 1);
    }

    @Test
    @Transactional
    public void assertThatUserMustExistToResetPassword() {
        playerRepository.saveAndFlush(player);
        Optional<Player> maybeUser = userService.requestPasswordReset("invalid.login@localhost");
        assertThat(maybeUser).isNotPresent();

        maybeUser = userService.requestPasswordReset(player.getEmail());
        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.orElse(null).getEmail()).isEqualTo(player.getEmail());
        assertThat(maybeUser.orElse(null).getResetDate()).isNotNull();
        assertThat(maybeUser.orElse(null).getResetKey()).isNotNull();
    }

    @Test
    @Transactional
    public void assertThatOnlyActivatedUserCanRequestPasswordReset() {
        player.setActivated(false);
        playerRepository.saveAndFlush(player);

        Optional<Player> maybeUser = userService.requestPasswordReset(player.getLogin());
        assertThat(maybeUser).isNotPresent();
        playerRepository.delete(player);
    }

    @Test
    @Transactional
    public void assertThatResetKeyMustNotBeOlderThan24Hours() {
        Instant daysAgo = Instant.now().minus(25, ChronoUnit.HOURS);
        String resetKey = RandomUtil.generateResetKey();
        player.setActivated(true);
        player.setResetDate(daysAgo);
        player.setResetKey(resetKey);
        playerRepository.saveAndFlush(player);

        Optional<Player> maybeUser = userService.completePasswordReset("johndoe2", player.getResetKey());
        assertThat(maybeUser).isNotPresent();
        playerRepository.delete(player);
    }

    @Test
    @Transactional
    public void assertThatResetKeyMustBeValid() {
        Instant daysAgo = Instant.now().minus(25, ChronoUnit.HOURS);
        player.setActivated(true);
        player.setResetDate(daysAgo);
        player.setResetKey("1234");
        playerRepository.saveAndFlush(player);

        Optional<Player> maybeUser = userService.completePasswordReset("johndoe2", player.getResetKey());
        assertThat(maybeUser).isNotPresent();
        playerRepository.delete(player);
    }

    @Test
    @Transactional
    public void assertThatUserCanResetPassword() {
        String oldPassword = player.getPassword();
        Instant daysAgo = Instant.now().minus(2, ChronoUnit.HOURS);
        String resetKey = RandomUtil.generateResetKey();
        player.setActivated(true);
        player.setResetDate(daysAgo);
        player.setResetKey(resetKey);
        playerRepository.saveAndFlush(player);

        Optional<Player> maybeUser = userService.completePasswordReset("johndoe2", player.getResetKey());
        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.orElse(null).getResetDate()).isNull();
        assertThat(maybeUser.orElse(null).getResetKey()).isNull();
        assertThat(maybeUser.orElse(null).getPassword()).isNotEqualTo(oldPassword);

        playerRepository.delete(player);
    }

    @Test
    @Transactional
    public void testFindNotActivatedUsersByCreationDateBefore() {
        Instant now = Instant.now();
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(now.minus(4, ChronoUnit.DAYS)));
        player.setActivated(false);
        Player dbPlayer = playerRepository.saveAndFlush(player);
        dbPlayer.setCreatedDate(now.minus(4, ChronoUnit.DAYS));
        playerRepository.saveAndFlush(player);
        List<Player> players = playerRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minus(3, ChronoUnit.DAYS));
        assertThat(players).isNotEmpty();
        userService.removeNotActivatedUsers();
        players = playerRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minus(3, ChronoUnit.DAYS));
        assertThat(players).isEmpty();

        // Verify Elasticsearch mock
        verify(mockUserSearchRepository, times(1)).delete(player);
    }

    private void generateUserToken(Player player, String tokenSeries, LocalDate localDate) {
        PersistentToken token = new PersistentToken();
        token.setSeries(tokenSeries);
        token.setPlayer(player);
        token.setTokenValue(tokenSeries + "-data");
        token.setTokenDate(localDate);
        token.setIpAddress("127.0.0.1");
        token.setUserAgent("Test agent");
        persistentTokenRepository.saveAndFlush(token);
    }

    @Test
    @Transactional
    public void assertThatAnonymousUserIsNotGet() {
        player.setLogin(Constants.ANONYMOUS_USER);
        if (!playerRepository.findOneByLogin(Constants.ANONYMOUS_USER).isPresent()) {
            playerRepository.saveAndFlush(player);
        }
        final PageRequest pageable = PageRequest.of(0, (int) playerRepository.count());
        final Page<UserDTO> allManagedUsers = userService.getAllManagedUsers(pageable);
        assertThat(allManagedUsers.getContent().stream()
            .noneMatch(user -> Constants.ANONYMOUS_USER.equals(user.getLogin())))
            .isTrue();
    }


    @Test
    @Transactional
    public void testRemoveNotActivatedUsers() {
        // custom "now" for audit to use as creation date
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(Instant.now().minus(30, ChronoUnit.DAYS)));

        player.setActivated(false);
        playerRepository.saveAndFlush(player);

        assertThat(playerRepository.findOneByLogin("johndoe")).isPresent();
        userService.removeNotActivatedUsers();
        assertThat(playerRepository.findOneByLogin("johndoe")).isNotPresent();

        // Verify Elasticsearch mock
        verify(mockUserSearchRepository, times(1)).delete(player);
    }

}
