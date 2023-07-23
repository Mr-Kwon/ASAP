// 현재 파일이 com.codingrecipe.member.dto 패키지에 포함되어 있음을 나타냅니다.
// 이 패키지는 주로 데이터 전송 객체(Data Transfer Object, DTO)를 포함하며, 계층 간 데이터 전달을 처리합니다.
package com.codingrecipe.member.dto;
// MemberEntity 클래스를 현재 파일에서 사용하기 위해 com.codingrecipe.member.entity 패키지에서 가져옵니다.
// 이 클래스는 데이터베이스의 한 테이블과 매핑되는 JPA 엔티티 클래스입니다.
import com.codingrecipe.member.entity.MemberEntity;
import lombok.*;

// @Getter: 각 필드에 대한 getter 메서드를 자동 생성합니다.
// @Setter: 각 필드에 대한 setter 메서드를 자동 생성합니다.
// @NoArgsConstructor: 매개변수가 없는 기본 생성자를 자동 생성합니다.
// @AllArgsConstructor: 모든 필드를 매개변수로 사용하는 생성자를 자동 생성합니다.
// @ToString: 클래스의 문자열 표현을 반환하는 toString 메서드를 자동 생성합니다.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberDTO {
    private Long id;
    private String memberEmail;
    private String memberPassword;
    private String memberName;

    // MemberEntity 타입의 하나의 매개변수 memberEntity를 입력으로 받고 MemberDTO 객체를 반환합니다.
    // 이 메서드의 목적은 MemberEntity 객체의 정보를 가져와 MemberDTO 객체로 변환하는 것입니다.
    public static MemberDTO toMemberDTO(MemberEntity memberEntity) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setId(memberEntity.getId());
        memberDTO.setMemberEmail(memberEntity.getMemberEmail());
        memberDTO.setMemberPassword(memberEntity.getMemberPassword());
        memberDTO.setMemberName(memberEntity.getMemberName());
        return memberDTO;
    }
}