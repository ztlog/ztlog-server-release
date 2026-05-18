package com.devlog.core.entity.content;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;


@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ContentTagPK implements Serializable {

    @Serial
    private static final long serialVersionUID = -8101983627354224421L;

    private Long tags;

    private Long contents;
}
