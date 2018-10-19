package pl.epoint.hackyeah.repository.search

import pl.epoint.hackyeah.domain.User
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

/**
 * Spring Data Elasticsearch repository for the User entity.
 */
interface UserSearchRepository : ElasticsearchRepository<User, Long>
