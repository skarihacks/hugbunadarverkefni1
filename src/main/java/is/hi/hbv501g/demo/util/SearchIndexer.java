package is.hi.hbv501g.demo.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SearchIndexer {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexer.class);

    private final List<IndexedPost> indexedPosts = Collections.synchronizedList(new ArrayList<>());

    public void indexPost(UUID id) {
        IndexedPost entry = new IndexedPost(id, LocalDateTime.now());
        indexedPosts.add(entry);
        log.debug("Indexed post {}", id);
    }

    public List<IndexedPost> getIndexedPosts() {
        return List.copyOf(indexedPosts);
    }

    public record IndexedPost(UUID id, LocalDateTime indexedAt) {}
}
