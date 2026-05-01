package com.cms.contactmanagement.service;

import com.cms.contactmanagement.entity.Contact;
import com.cms.contactmanagement.entity.User;
import com.cms.contactmanagement.exception.ContactNotFoundException;
import com.cms.contactmanagement.exception.OwnershipViolationException;
import com.cms.contactmanagement.repository.ContactRepository;
import com.cms.contactmanagement.repository.UserRepository;
import com.cms.contactmanagement.service.impl.ContactServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactServiceImpl contactService;

    @BeforeEach
    void setUpAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", null, List.of())
        );

        User user = new User();
        user.setId(42L);
        user.setEmail("user@example.com");
        when(userRepository.findByEmail(eq("user@example.com"))).thenReturn(Optional.of(user));
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createContact_savesAndReturnsCorrectData() {
        Contact toSave = new Contact();
        toSave.setFirstName("John");
        toSave.setLastName("Smith");
        toSave.setTitle("Mr");

        Contact saved = new Contact();
        saved.setId(100L);
        saved.setUserId(42L);
        saved.setFirstName("John");
        saved.setLastName("Smith");
        saved.setTitle("Mr");

        when(contactRepository.save(any(Contact.class))).thenReturn(saved);

        Contact result = contactService.createContact(toSave);

        assertEquals(100L, result.getId());
        assertEquals(42L, result.getUserId());
        assertEquals("John", result.getFirstName());

        ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);
        verify(contactRepository).save(captor.capture());
        assertNull(captor.getValue().getId());
        assertEquals(42L, captor.getValue().getUserId());
    }

    @Test
    void getContactById_throwsContactNotFoundException_whenIdNotFound() {
        when(contactRepository.findById(eq(1L))).thenReturn(Optional.empty());
        assertThrows(ContactNotFoundException.class, () -> contactService.getContactById(1L));
        verify(contactRepository).findById(1L);
    }

    @Test
    void getAllContacts_returnsCorrectPaginatedResult() {
        PageRequest pr = PageRequest.of(0, 2);
        Contact c1 = new Contact();
        c1.setId(1L);
        c1.setUserId(42L);
        Contact c2 = new Contact();
        c2.setId(2L);
        c2.setUserId(42L);

        when(contactRepository.findAllByUserId(eq(42L), eq(pr)))
                .thenReturn(new PageImpl<>(List.of(c1, c2), pr, 2));

        Page<Contact> page = contactService.getAllContacts(pr);

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
        verify(contactRepository).findAllByUserId(42L, pr);
    }

    @Test
    void updateContact_throwsException_whenContactBelongsToDifferentUser() {
        Contact existing = new Contact();
        existing.setId(5L);
        existing.setUserId(999L);

        when(contactRepository.findById(eq(5L))).thenReturn(Optional.of(existing));

        Contact update = new Contact();
        update.setFirstName("New");
        update.setLastName("Name");

        assertThrows(OwnershipViolationException.class, () -> contactService.updateContact(5L, update));
        verify(contactRepository).findById(5L);
        verify(contactRepository, never()).save(any());
    }

    @Test
    void deleteContact_happyPath() {
        Contact existing = new Contact();
        existing.setId(10L);
        existing.setUserId(42L);

        when(contactRepository.findById(eq(10L))).thenReturn(Optional.of(existing));

        assertDoesNotThrow(() -> contactService.deleteContact(10L));

        verify(contactRepository).delete(existing);
    }
}

