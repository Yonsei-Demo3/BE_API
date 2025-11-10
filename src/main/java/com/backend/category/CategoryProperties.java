package com.backend.category;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties
@Getter
@Setter
public class CategoryProperties {

    private List<CategoryNode> categories = new ArrayList<>();

    @Getter
    @Setter
    public static class CategoryNode {
        private String name;
        private List<String> children = new ArrayList<>();
    }
}
