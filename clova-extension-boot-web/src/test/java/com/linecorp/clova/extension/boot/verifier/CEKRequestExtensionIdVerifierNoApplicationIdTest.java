/*
 * Copyright 2018 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.clova.extension.boot.verifier;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.Configuration;

import com.linecorp.clova.extension.boot.handler.annnotation.CEKRequestHandler;
import com.linecorp.clova.extension.boot.handler.annnotation.IntentMapping;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.test.CEKRequestGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "cek.verifier.extension-id.enabled=true",
        "cek.verifier.extension-id.id="
})
public class CEKRequestExtensionIdVerifierNoApplicationIdTest {

    @TestConfiguration
    static class TestConfig {

        @CEKRequestHandler
        static class TestHandler {

            @IntentMapping("CEKRequestExtensionIdVerifierNoApplicationIdTest")
            CEKResponse handleExtensionIdVerifierTest() {
                return CEKResponse.empty();
            }
        }

    }

    @Autowired
    MockMvc mvc;

    @SpyBean
    TestConfig.TestHandler handler;

    @Autowired
    Configuration configuration;

    @Test
    public void test_noApplicationId() throws Throwable {
        String content = CEKRequestGenerator
                .requestBodyBuilder("data/request.json", configuration)
                .intent("CEKRequestExtensionIdVerifierNoApplicationIdTest")
                .remove("$.context.System.application.applicationId")
                .build();

        mvc.perform(post("/cek/v1")
                            .content(content)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleExtensionIdVerifierTest();
    }

    @Test
    public void test_wrongApplicationId() throws Throwable {
        String applicationId = UUID.randomUUID().toString();

        final String content = CEKRequestGenerator
                .requestBodyBuilder("data/request.json", configuration)
                .intent("CEKRequestExtensionIdVerifierNoApplicationIdTest")
                .placeholder("applicationId", applicationId)
                .build();

        mvc.perform(post("/cek/v1")
                            .content(content)
                            .contentType(MediaType.APPLICATION_JSON))
           .andDo(print())
           .andExpect(status().isOk());

        verify(handler).handleExtensionIdVerifierTest();
    }

}
