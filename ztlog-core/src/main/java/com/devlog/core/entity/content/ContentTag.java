package com.devlog.core.entity.content;

import com.devlog.core.entity.tag.Tag;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@IdClass(ContentTagPK.class)
@Table(name = "contents_tags")
public class ContentTag {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TAG_NO", nullable = false)
    private Tag tags;

    @Column(name = "SORT", nullable = false)
    private Integer sort;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CTNT_NO", nullable = false)
    private Content contents;

    public static ContentTag created(Tag tag, int sort, Content content) {
        return ContentTag.builder()
                .tags(tag)
                .sort(sort)
                .contents(content)
                .build();
    }

    public void updated(Tag tag, int sort) {
        this.tags = tag;
        this.sort = sort;
    }
}
