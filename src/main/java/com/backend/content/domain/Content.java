package com.backend.content.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contents")
@Getter
@NoArgsConstructor
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String creator;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(length = 1000)
    private String link;
    //TODO: Base Entity 연결, 카테고리 기능 구현 후 연결

    @Builder
    private Content(String name, String creator, String description, String image_url, String link) {
        this.name = name;
        this.creator = creator;
        this.description = description;
        this.imageUrl = image_url;
        this.link = link;
    }

    public static Content of(String name, String creator, String description, String image_url, String link) {
        return new Content(name, creator, description, image_url, link);
    }

}
