package kr.co.doritos.controller;

import javax.servlet.http.HttpServletRequest;

import kr.co.doritos.domain.Team;
import kr.co.doritos.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import kr.co.doritos.common.ResponseCode;
import kr.co.doritos.domain.Member;
import kr.co.doritos.exception.ChatException;
import kr.co.doritos.service.MemberService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * HomeController
 */
@Controller
@Slf4j
public class HomeController {

	private final MemberService memberService;
	private final TeamService teamService;

	@Autowired
	public HomeController(MemberService memberService, TeamService teamService) {
		this.memberService = memberService;
		this.teamService = teamService;
	}
	
	@GetMapping(value="/")
	public ModelAndView index(HttpServletRequest request) {
		
		String ip = request.getRemoteAddr();
		
		ModelAndView mav = new ModelAndView();
		
		Member member = null;
		
		try {
			
			member = memberService.findByIp(ip);
			List<Team> teamList = teamService.findAll();
			
			mav.addObject("member", member);
			mav.addObject("teamList", teamList);

			mav.setViewName("index");
			
		} catch(ChatException e) {
			log.error("[{}][{}] {}", ip, e.getResponseCode(), e.getMessage());
			mav.addObject("message", e.getMessage());
			mav.setViewName("error");
		} catch(Exception e) {
			log.error("[{}][{}] {}", ip, ResponseCode.R9999, e.getMessage());
			mav.addObject("message", e.getMessage());
			mav.setViewName("error");
		} finally {
			log.trace("[{}] 링크챗 접속.", member != null ? member.getName() : ip);
		}
		
		return mav;
	}
}
