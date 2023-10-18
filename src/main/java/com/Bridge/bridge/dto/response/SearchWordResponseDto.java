package com.Bridge.bridge.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchWordResponseDto {

    Long searchWordId;

    String searchWord; // 검색어 내용

    @Builder
    public SearchWordResponseDto(Long searchWordId, String searchWord) {
        this.searchWordId = searchWordId;
        this.searchWord = searchWord;
    }
}
