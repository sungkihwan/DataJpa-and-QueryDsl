package study.datajpa.dto;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Data;

@Data
public class MemberSearchCondition {
    private String username;
    private String teamname;
    private Integer ageGoe;
    private Integer ageLoe;
}
