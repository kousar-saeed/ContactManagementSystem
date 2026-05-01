package com.cms.contactmanagement.controller;

import com.cms.contactmanagement.dto.ContactRequestDto;
import com.cms.contactmanagement.dto.ContactResponseDto;
import com.cms.contactmanagement.dto.EmailAddressResponseDto;
import com.cms.contactmanagement.dto.PhoneNumberResponseDto;
import com.cms.contactmanagement.entity.Contact;
import com.cms.contactmanagement.entity.EmailAddress;
import com.cms.contactmanagement.entity.PhoneNumber;
import com.cms.contactmanagement.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactResponseDto> create(@Valid @RequestBody ContactRequestDto request) {
        log.info("Create contact request received");
        Contact saved = contactService.createContact(toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<Page<ContactResponseDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Contact> result = (search == null || search.isBlank())
                ? contactService.getAllContacts(pageable)
                : contactService.searchContacts(search, pageable);

        return ResponseEntity.ok(result.map(this::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponseDto> getById(@PathVariable Long id) {
        Contact contact = contactService.getContactById(id);
        return ResponseEntity.ok(toResponse(contact));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ContactRequestDto request
    ) {
        Contact updated = contactService.updateContact(id, toEntity(request));
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }

    private Contact toEntity(ContactRequestDto dto) {
        Contact c = new Contact();
        c.setFirstName(dto.getFirstName());
        c.setLastName(dto.getLastName());
        c.setTitle(dto.getTitle());

        List<EmailAddress> emails = dto.getEmailAddresses().stream().map(e -> {
            EmailAddress entity = new EmailAddress();
            entity.setEmail(e.getEmail());
            entity.setLabel(e.getLabel());
            entity.setContact(c);
            return entity;
        }).toList();

        List<PhoneNumber> phones = dto.getPhoneNumbers().stream().map(p -> {
            PhoneNumber entity = new PhoneNumber();
            entity.setNumber(p.getNumber());
            entity.setLabel(p.getLabel());
            entity.setContact(c);
            return entity;
        }).toList();

        c.getEmailAddresses().clear();
        c.getEmailAddresses().addAll(emails);

        c.getPhoneNumbers().clear();
        c.getPhoneNumbers().addAll(phones);

        return c;
    }

    private ContactResponseDto toResponse(Contact c) {
        return ContactResponseDto.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .title(c.getTitle())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .emailAddresses(c.getEmailAddresses().stream()
                        .map(e -> EmailAddressResponseDto.builder()
                                .id(e.getId())
                                .email(e.getEmail())
                                .label(e.getLabel())
                                .build())
                        .toList())
                .phoneNumbers(c.getPhoneNumbers().stream()
                        .map(p -> PhoneNumberResponseDto.builder()
                                .id(p.getId())
                                .number(p.getNumber())
                                .label(p.getLabel())
                                .build())
                        .toList())
                .build();
    }
}

