package com.gamesearch.domain.community;

import com.gamesearch.domain.user.User; // User 엔티티를 사용하기 위해 import

public class CommunityMapper {
    public static CommunityDto toDto(Community community) {
        CommunityDto dto = new CommunityDto();
        dto.setId(community.getId());
        dto.setTitle(community.getTitle());
        dto.setDescription(community.getDescription());
        dto.setCategory(community.getCategory());
        
        // 작성자 정보 설정
        User author = community.getAuthor(); // 작성자 가져오기
        dto.setAuthorName(author.getNickname()); // 닉네임
        dto.setAuthorEmail(author.getEmail());   // 이메일 추가
        dto.setAuthorId(author.getId());         // 작성자 ID 추가
        
        dto.setDate(community.getDate());
        dto.setViewCount(community.getViewCount());
        dto.setCommentCount(community.getCommentCount());
        
        // // 댓글 수 설정 (댓글 리스트가 필요함)
        // if (community.getComments() != null) {
        //     dto.setCommentCount(community.getComments().size()); // 댓글 수 설정
        // } else {
        //     dto.setCommentCount(0); // 댓글이 없을 경우 0으로 설정
        // }

        return dto;
    }

    public static Community toEntity(CommunityDto dto) {
        Community community = new Community();
        community.setId(dto.getId());
        community.setCategory(dto.getCategory());
        community.setTitle(dto.getTitle());
        community.setDescription(dto.getDescription());
        community.setViewCount(dto.getViewCount());
        
        // author와 date는 별도로 설정해야 함
        // 작성자는 서비스 레이어에서 설정할 수 있도록 변경합니다.
        
        return community;
    }
}