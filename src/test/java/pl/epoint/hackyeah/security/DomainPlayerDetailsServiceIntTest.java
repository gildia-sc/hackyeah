package pl.epoint.hackyeah.security;

import pl.epoint.hackyeah.HackyeahApp;
import pl.epoint.hackyeah.domain.Player;
import pl.epoint.hackyeah.repository.PlayerRepository;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for DomainUserDetailsService.
 *
 * @see DomainUserDetailsService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HackyeahApp.class)
@Transactional
public class DomainPlayerDetailsServiceIntTest {

    private static final String USER_ONE_LOGIN = "test-user-one";
    private static final String USER_ONE_EMAIL = "test-user-one@localhost";
    private static final String USER_TWO_LOGIN = "test-user-two";
    private static final String USER_TWO_EMAIL = "test-user-two@localhost";
    private static final String USER_THREE_LOGIN = "test-user-three";
    private static final String USER_THREE_EMAIL = "test-user-three@localhost";

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private UserDetailsService domainUserDetailsService;

    private Player playerOne;
    private Player playerTwo;
    private Player playerThree;

    @Before
    public void init() {
        playerOne = new Player();
        playerOne.setLogin(USER_ONE_LOGIN);
        playerOne.setPassword(RandomStringUtils.random(60));
        playerOne.setActivated(true);
        playerOne.setEmail(USER_ONE_EMAIL);
        playerOne.setFirstName("userOne");
        playerOne.setLastName("doe");
        playerOne.setLangKey("en");
        playerRepository.save(playerOne);

        playerTwo = new Player();
        playerTwo.setLogin(USER_TWO_LOGIN);
        playerTwo.setPassword(RandomStringUtils.random(60));
        playerTwo.setActivated(true);
        playerTwo.setEmail(USER_TWO_EMAIL);
        playerTwo.setFirstName("userTwo");
        playerTwo.setLastName("doe");
        playerTwo.setLangKey("en");
        playerRepository.save(playerTwo);

        playerThree = new Player();
        playerThree.setLogin(USER_THREE_LOGIN);
        playerThree.setPassword(RandomStringUtils.random(60));
        playerThree.setActivated(false);
        playerThree.setEmail(USER_THREE_EMAIL);
        playerThree.setFirstName("userThree");
        playerThree.setLastName("doe");
        playerThree.setLangKey("en");
        playerRepository.save(playerThree);
    }

    @Test
    @Transactional
    public void assertThatUserCanBeFoundByLogin() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    }

    @Test
    @Transactional
    public void assertThatUserCanBeFoundByLoginIgnoreCase() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_LOGIN.toUpperCase(Locale.ENGLISH));
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    }

    @Test
    @Transactional
    public void assertThatUserCanBeFoundByEmail() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_TWO_EMAIL);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_TWO_LOGIN);
    }

    @Test(expected = UsernameNotFoundException.class)
    @Transactional
    public void assertThatUserCanNotBeFoundByEmailIgnoreCase() {
    domainUserDetailsService.loadUserByUsername(USER_TWO_EMAIL.toUpperCase(Locale.ENGLISH));
    }

    @Test
    @Transactional
    public void assertThatEmailIsPrioritizedOverLogin() {
        UserDetails userDetails = domainUserDetailsService.loadUserByUsername(USER_ONE_EMAIL);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    }

    @Test(expected = UserNotActivatedException.class)
    @Transactional
    public void assertThatUserNotActivatedExceptionIsThrownForNotActivatedUsers() {
        domainUserDetailsService.loadUserByUsername(USER_THREE_LOGIN);
    }

}
