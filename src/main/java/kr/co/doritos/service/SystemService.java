package kr.co.doritos.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kr.co.doritos.common.AccessType;
import kr.co.doritos.common.ResponseCode;
import kr.co.doritos.common.UseStatus;
import kr.co.doritos.domain.*;
import kr.co.doritos.exception.ChatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemService {

    private final ChatRoomService chatRoomService;
    private final TeamService teamService;
    private final MemberService memberService;

    @Value("${service.auto-room}")
    private String autoRoom;

    @PostConstruct
    public void initialize() {

        if (!"Y".equals(autoRoom)) {
            log.trace("[SYSTEM] ChatRoom None... {}", chatRoomService.findAll());
            return;
        }

        try {
            initTeam();
            initMember();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        List<Team> teamList = teamService.findAll();
        List<Member> memberList = memberService.findAll();

        // 팀이 1개인 경우, 전체방 생성하지 않음.
        if (teamList.size() == 1) {
            initTeamAndPartChatRoom(teamList.get(0), memberList);
            return;
        }

        // 전체방 생성
        initAllChatRoom(memberList);

        // 팀별 방 생성
        for (Team team : teamList) {
            initTeamAndPartChatRoom(team, memberList);
        }

        log.trace("[SYSTEM] ChatRoom Memory Load... {}", chatRoomService.findAll());
    }

    private void initTeam() throws Exception {
        File configFile = new File("team.xml");
        if (!configFile.exists()) {
            throw new ChatException(ResponseCode.R9998, String.format("%s 를 찾을 수 없습니다.", "team.xml"));
        }

        ObjectMapper mapper = new XmlMapper();
        List<Team> teamList = mapper.readValue(configFile, new TypeReference<>() {});

        teamList.stream()
            .forEach(e -> {
                teamService.save(e);
            });

        log.trace("[TEAM] Memory Load... {}", teamService.findAll());
    }

    private void initMember() throws Exception {
        File configFile = new File("member.xml");

        if (!configFile.exists()) {
            throw new ChatException(ResponseCode.R9998, String.format("%s 를 찾을 수 없습니다.", "member.xml"));
        }

        ObjectMapper mapper = new XmlMapper();
        List<Member> memberList = mapper.readValue(configFile, new TypeReference<>() {});

        memberList.stream()
            .forEach(e -> {
                String teamId = e.getTeamId();
                String partValue = e.getPartValue();
                Team team = teamService.findAll().stream().filter(ee -> ee.getId().equals(teamId)).findFirst().get();
                Part part = team.getPartList().stream().filter(ee -> ee.getValue().equals(partValue)).findFirst().get();
                e.setTeam(team);
                e.setPart(part);
                memberService.save(e);
            });

        log.trace("[MEMBER] Memory Load... {}", memberService.findAll());
    }

    private void initAllChatRoom(List<Member> memberList) {
        // 전체멤버 채팅방
        chatRoomService.save(ChatRoom.builder()
                .id("ALL")
                .creator(Member.builder().ip("127.0.0.1").name("SYSTEM").build())
                .logViewYn("Y")
                .accessType(AccessType.ANY)
                .name("[전체] 채팅방")
                .status(UseStatus.Y)
                .accessMemberList(memberList)
                .messageList(new ArrayList<Message>())
                .liveMemberList(new ArrayList<>())
                .build());
    }

    private void initTeamAndPartChatRoom(Team team, List<Member> memberList) {
        List<Member> teamMemberList = memberList.stream()
                .filter(e -> e.getTeamId().equals(team.getId()))
                .collect(Collectors.toList());

        // 팀 전체방
        chatRoomService.save(ChatRoom.builder()
                .id(team.getId())
                .creator(Member.builder().ip("127.0.0.1").name("SYSTEM").build())
                .logViewYn("Y")
                .accessType(AccessType.PROTECTED)
                .name(String.format("[%s] 채팅방", team.getName()))
                .status(UseStatus.Y)
                .accessMemberList(teamMemberList)
                .messageList(new ArrayList<Message>())
                .liveMemberList(new ArrayList<>())
                .build());

        // 파트 2개 이상 존재할 시, 파트방 생성
        List<Part> partList = team.getPartList();
        if (partList != null && partList.size() > 1) {
            for (Part part : partList) {
                List<Member> partMemberList = teamMemberList.stream()
                        .filter(e -> e.getPartValue().equals(part.getValue()))
                        .collect(Collectors.toList());

                chatRoomService.save(ChatRoom.builder()
                        .id(String.format("%s-%s", team.getId(), part.getValue()))
                        .creator(Member.builder().ip("127.0.0.1").name("SYSTEM").build())
                        .logViewYn("Y")
                        .accessType(AccessType.PROTECTED)
                        .name(String.format("[%s(%s)] 채팅방", team.getName(), part.getValue()))
                        .status(UseStatus.Y)
                        .accessMemberList(partMemberList)
                        .messageList(new ArrayList<Message>())
                        .liveMemberList(new ArrayList<>())
                        .build());
            }
        }
    }
}