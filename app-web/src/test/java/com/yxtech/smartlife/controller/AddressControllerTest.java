package com.yxtech.smartlife.controller;

import com.yxtech.smartlife.TestWebApplication;
import com.yxtech.smartlife.entity.AddressOption;
import com.yxtech.smartlife.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = TestWebApplication.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;

    @Test
    void getAddressTreeShouldReturnNestedAddressOptions() throws Exception {
        AddressOption option = new AddressOption();
        option.setCity("杭州");
        option.setDistrict("滨江区");
        option.setStreet("长河街道");
        option.setCommunityName("卓悦华庭");
        when(addressService.findAllOptions()).thenReturn(List.of(option));

        mockMvc.perform(get("/api/addresses/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].label").value("杭州"))
                .andExpect(jsonPath("$.data[0].children[0].label").value("滨江区"))
                .andExpect(jsonPath("$.data[0].children[0].children[0].label").value("长河街道"))
                .andExpect(jsonPath("$.data[0].children[0].children[0].children[0].label").value("卓悦华庭"));
    }
}
