package com.ericsson.graduates.team1.jenkinsinfo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JenkinsinfoApplicationTests {

	@Autowired
	private JenkinsController jenkinsController;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
		assertThat(jenkinsController).isNotNull();
    }

    @Test
	void testHomepage() throws Exception {
		this.mockMvc.perform(get("/")).andExpect(status().isOk())
				.andExpect(content().string(containsString("eric-oss-data-catalog_PreCodeReview")));
	}

	@Test
	void testPipeline() throws Exception {
		this.mockMvc.perform(post("/getRepository")
				.param("repositoryName", "Admin_create_new_project")
				.param("period", "day")
				.flashAttr("response", new JenkinsResponseObject()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("<td>Admin_create_new_project</td>")));
	}

}
