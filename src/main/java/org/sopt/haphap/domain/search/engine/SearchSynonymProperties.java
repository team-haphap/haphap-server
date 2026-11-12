package org.sopt.haphap.domain.search.engine;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "search")
public class SearchSynonymProperties {

    private List<List<String>> synonyms = new ArrayList<>();
}