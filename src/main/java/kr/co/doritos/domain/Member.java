package kr.co.doritos.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

	private String ip;
	private String name;
	private String teamId;
	private String partValue;
	private Team team;
	private Part part;
}
