package com.devlog.core.entity.tag;

import com.devlog.core.entity.content.ContentTag;
import com.devlog.core.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "tags_mst")
public class Tag extends BaseTimeEntity {

    @OneToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private List<ContentTag> contentTags = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TAG_NO", nullable = false)
    private Long tagNo;

    @Column(name = "TAG_NAME", nullable = false)
    private String tagName;

    public static Tag created(String tagName) {
        return Tag.builder()
                .tagName(tagName)
                .build();
    }

    public void updated(String tagName) {
        this.tagName = tagName;
    }

}
