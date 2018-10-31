package pl.epoint.hackyeah.repository.search

import pl.epoint.hackyeah.domain.Player
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

/**
 * Spring Data Elasticsearch repository for the User entity.
 */
interface UserSearchRepository : ElasticsearchRepository<Player, Long>
