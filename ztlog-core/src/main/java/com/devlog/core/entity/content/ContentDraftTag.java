package com.devlog.core.entity.content;

import com.devlog.core.entity.tag.Tag;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "contents_draft_tags")
public class ContentDraftTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DRAFT_TAG_NO", nullable = false)
    private Long draftTagNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DRAFT_NO", nullable = false)
    private ContentDraft drafts;

    // 초안 저장 시점에는 아직 tags_mst에 없는 새 태그명일 수 있으므로 tags는 nullable —
    // 발행 시점에 비로소 태그가 생성되면 그때 채워진다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TAG_NO")
    private Tag tags;

    @Column(name = "TAG_NAME", nullable = false)
    private String tagName;

    @Column(name = "SORT", nullable = false)
    private Integer sort;

    public static ContentDraftTag created(ContentDraft draft, Tag tag, String tagName, int sort) {
        return ContentDraftTag.builder()
                .drafts(draft)
                .tags(tag)
                .tagName(tagName)
                .sort(sort)
                .build();
    }

    public void updated(Tag tag, String tagName, int sort) {
        this.tags = tag;
        this.tagName = tagName;
        this.sort = sort;
    }
}