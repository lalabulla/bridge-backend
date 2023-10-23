package com.Bridge.bridge.service;

import com.Bridge.bridge.domain.Alarm;
import com.Bridge.bridge.domain.Platform;
import com.Bridge.bridge.domain.User;
import com.Bridge.bridge.dto.response.AllAlarmResponse;
import com.Bridge.bridge.repository.AlarmRepository;
import com.Bridge.bridge.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class AlarmServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private AlarmService alarmService;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
        alarmRepository.deleteAll();
    }

    @DisplayName("모든 알람 조회")
    @Test
    void getAllAlarm() {
        // given
        User user = new User("user", "user@gmaiil.com", Platform.APPLE, "alarm");
        userRepository.save(user);

        Alarm alarm1 = Alarm.builder()
                .type("Applier")
                .title("지원자 등장?")
                .content("내 프로젝트에 누군가 지원했어요 지원자 프로필을 확인하고 채팅을 시작해보세요!")
                .rcvUser(user)
                .build();
        Alarm alarm2 = Alarm.builder()
                .type("Applier")
                .title("지원자 등장?")
                .content("내 프로젝트에 누군가 지원했어요 지원자 프로필을 확인하고 채팅을 시작해보세요!")
                .rcvUser(user)
                .build();
        Alarm alarm3 = Alarm.builder()
                .type("Apply")
                .title("지원 결과 도착")
                .content("내가 지원한 프로젝트의 결과가 나왔어요. 관리 페이지에서 확인해보세요.")
                .rcvUser(user)
                .build();

        alarmRepository.save(alarm1);
        alarmRepository.save(alarm2);
        alarmRepository.save(alarm3);

        // when
        List<AllAlarmResponse> responses = alarmService.getAllOfAlarms(user.getId());

        // then
        Assertions.assertThat(responses.size()).isEqualTo(3);
        Assertions.assertThat(responses.get(0).getTitle()).isEqualTo("지원자 등장?");
        Assertions.assertThat(responses.get(1).getTitle()).isEqualTo("지원자 등장?");
        Assertions.assertThat(responses.get(2).getTitle()).isEqualTo("지원 결과 도착");

    }

}
