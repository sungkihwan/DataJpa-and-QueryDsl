package study.datajpa.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import study.datajpa.entity.Member;

@Data
@NoArgsConstructor
public class MemberDto {

    private Long id;
    private String username;
    private int age;
    private String teamname;

    public MemberDto(Long id, String username, String teamname) {
        this.id = id;
        this.username = username;
        this.teamname = teamname;
    }

    public MemberDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
    }

    @QueryProjection
    public MemberDto(String username, int age) {
        this.age = age;
        this.username = username;
    }
}
