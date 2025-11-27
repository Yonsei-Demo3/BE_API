package com.backend.roomMember.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;

public final class RandomNicknameGenerator {

    private static final List<String> ADJS = List.of(
        "따뜻한", "기분좋은", "상냥한", "다정한", "사랑스러운", "활기찬", "씩씩한", "용감한",
        "든든한", "친절한", "귀여운", "산뜻한", "유쾌한", "신나는", "환한", "평온한", "포근한",
        "똑똑한", "영리한", "재밌는", "명랑한", "청량한", "마음넓은", "섬세한", "진솔한",
        "담백한", "믿음직한", "선한", "배려하는", "부드러운", "은은한", "온화한", "감동적인",
        "기특한", "당당한", "시원한", "희망찬", "뛰어난", "반짝이는", "순수한", "성실한",
        "정직한", "활발한", "열정적인", "적극적인", "듬직한", "깔끔한", "정다운", "기쁜",
        "찬란한", "유능한", "다채로운", "평화로운", "환상적인", "여유로운", "균형잡힌",
        "가치있는", "특별한", "소중한", "센스있는", "흐뭇한", "신비로운", "감미로운",
        "재미있는", "생기있는", "조용한", "단정한", "향긋한", "싱그러운", "깊은", "화사한",
        "해맑은", "빛나는", "안정적인", "혁신적인", "풍성한", "담력있는", "소박한", "순한",
        "자유로운", "친근한", "깨끗한", "의젓한", "당찬", "스윗한", "활짝핀", "청아한",
        "푸른", "금빛의", "예쁜", "다복한", "반가운", "잔잔한", "귀염뽀짝한", "훈훈한", "상쾌한"
    );

    private static final List<String> ANIMALS = List.of(
        "강아지", "고양이", "토끼", "여우", "사슴", "판다", "코알라", "호랑이", "사자", "치타",
        "기린", "하마", "코끼리", "원숭이", "오랑우탄", "돌고래", "고래", "상어", "물개",
        "펭귄", "부엉이", "올빼미", "참새", "독수리", "매", "까치", "비둘기", "까마귀",
        "타조", "알파카", "라마", "양", "염소", "소", "말", "돼지", "두더지", "다람쥐",
        "고슴도치", "두꺼비", "개구리", "이구아나", "카멜레온", "거북이", "악어", "해달",
        "수달", "비버", "오소리", "족제비", "너구리", "미어캣", "하이에나", "바분",
        "카피바라", "바다사자", "가오리", "문어", "오징어", "게", "가재", "새우", "백호",
        "흑표범", "재규어", "퓨마", "북극곰", "여우원숭이", "사막여우", "진돗개", "삽살개",
        "웰시코기", "골든리트리버", "알래스칸허스키", "래브라도", "치와와", "푸들", "말티즈",
        "셰퍼드", "보더콜리", "비글", "러시안블루", "벵갈고양이", "스핑크스", "샴고양이",
        "메인쿤", "스코티시폴드", "하늘다람쥐", "흰돌고래", "바다표범"
    );

    public static String generate(Set<String> existingNicknames) {
        String nickname;

        do {
            int adjIndex = ThreadLocalRandom.current().nextInt(ADJS.size());
            int animalIndex = ThreadLocalRandom.current().nextInt(ANIMALS.size());

            String adj = ADJS.get(adjIndex);
            String animal = ANIMALS.get(animalIndex);

            nickname = adj + " " + animal;
        } while (existingNicknames.contains(nickname));

        return nickname;
    }
}
