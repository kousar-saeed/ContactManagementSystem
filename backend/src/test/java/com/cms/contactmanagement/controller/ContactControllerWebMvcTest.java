package com.cms.contactmanagement.controller;

import com.cms.contactmanagement.entity.Contact;
import com.cms.contactmanagement.exception.ContactNotFoundException;
import com.cms.contactmanagement.exception.GlobalExceptionHandler;
import com.cms.contactmanagement.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cms.contactmanagement.config.JwtAuthenticationFilter;
import com.cms.contactmanagement.config.JwtUtil;

@WebMvcTest(controllers = ContactController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ContactControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void getContacts_returns200_withPaginatedResponse() throws Exception {
        Contact c1 = new Contact();
        c1.setId(1L);
        c1.setFirstName("John");
        c1.setLastName("Smith");
        c1.setTitle("Mr");

        Contact c2 = new Contact();
        c2.setId(2L);
        c2.setFirstName("Jane");
        c2.setLastName("Doe");
        c2.setTitle("Ms");

        PageRequest pr = PageRequest.of(0, 10);
        when(contactService.getAllContacts(any())).thenReturn(new PageImpl<>(List.of(c1, c2), pr, 2));

        mockMvc.perform(get("/api/contacts?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void postContacts_returns201() throws Exception {
        Contact saved = new Contact();
        saved.setId(10L);
        saved.setFirstName("John");
        saved.setLastName("Smith");
        saved.setTitle("Mr");

        when(contactService.createContact(any(Contact.class))).thenReturn(saved);

        mockMvc.perform(post("/api/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "John",
                                  "lastName": "Smith",
                                  "title": "Mr",
                                  "emailAddresses": [
                                    { "email": "john.smith@work.com", "label": "WORK" }
                                  ],
                                  "phoneNumbers": [
                                    { "number": "1234567890", "label": "PERSONAL" }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.emailAddresses").isArray())
                .andExpect(jsonPath("$.phoneNumbers").isArray());
    }

    @Test
    void deleteContact_returns204() throws Exception {
        doNothing().when(contactService).deleteContact(anyLong());

        mockMvc.perform(delete("/api/contacts/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getContactById_returns404_whenNotFound() throws Exception {
        when(contactService.getContactById(99L)).thenThrow(new ContactNotFoundException("Contact not found"));

        mockMvc.perform(get("/api/contacts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}

