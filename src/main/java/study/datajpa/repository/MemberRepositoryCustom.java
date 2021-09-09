package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.datajpa.dto.MemberDto;
import study.datajpa.dto.MemberSearchCondition;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberDto> search(MemberSearchCondition cond);
    Page<MemberDto> searchPageSimple(MemberSearchCondition cond, Pageable pageable);
    Page<MemberDto> searchPageComplex(MemberSearchCondition cond, Pageable pageable);
}
