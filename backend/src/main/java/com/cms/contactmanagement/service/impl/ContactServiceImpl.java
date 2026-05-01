package com.cms.contactmanagement.service.impl;

import com.cms.contactmanagement.entity.Contact;
import com.cms.contactmanagement.entity.User;
import com.cms.contactmanagement.exception.ContactNotFoundException;
import com.cms.contactmanagement.exception.OwnershipViolationException;
import com.cms.contactmanagement.exception.UserNotFoundException;
import com.cms.contactmanagement.repository.ContactRepository;
import com.cms.contactmanagement.repository.UserRepository;
import com.cms.contactmanagement.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Contact createContact(Contact contact) {
        Long userId = currentUserId();
        log.info("Creating contact for userId={}", userId);

        contact.setId(null);
        contact.setUserId(userId);

        Contact saved = contactRepository.save(contact);
        log.info("Contact created: contactId={}, userId={}", saved.getId(), userId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Contact getContactById(Long contactId) {
        Long userId = currentUserId();
        log.info("Fetching contact: contactId={}, userId={}", contactId, userId);

        return getOwnedContactOrThrow(contactId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contact> getAllContacts(Pageable pageable) {
        Long userId = currentUserId();
        log.info("Listing contacts: userId={}, page={}, size={}", userId, pageable.getPageNumber(), pageable.getPageSize());
        return contactRepository.findAllByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contact> searchContacts(String firstName, String lastName, Pageable pageable) {
        Long userId = currentUserId();
        String fn = normalizeBlankToNull(firstName);
        String ln = normalizeBlankToNull(lastName);
        log.info("Searching contacts: userId={}, firstName='{}', lastName='{}', page={}, size={}",
                userId, fn, ln, pageable.getPageNumber(), pageable.getPageSize());
        return contactRepository.searchByUserIdAndName(userId, fn, ln, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contact> searchContacts(String term, Pageable pageable) {
        Long userId = currentUserId();
        String t = normalizeBlankToNull(term);
        log.info("Searching contacts by term: userId={}, term='{}', page={}, size={}",
                userId, t, pageable.getPageNumber(), pageable.getPageSize());
        return contactRepository.searchByUserIdAndTerm(userId, t, pageable);
    }

    @Override
    @Transactional
    public Contact updateContact(Long contactId, Contact updated) {
        Long userId = currentUserId();
        log.info("Updating contact: contactId={}, userId={}", contactId, userId);

        Contact existing = getOwnedContactOrThrow(contactId, userId);

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setTitle(updated.getTitle());

        existing.getEmailAddresses().clear();
        existing.getEmailAddresses().addAll(updated.getEmailAddresses());
        existing.getEmailAddresses().forEach(e -> e.setContact(existing));

        existing.getPhoneNumbers().clear();
        existing.getPhoneNumbers().addAll(updated.getPhoneNumbers());
        existing.getPhoneNumbers().forEach(p -> p.setContact(existing));

        Contact saved = contactRepository.save(existing);
        log.info("Contact updated: contactId={}, userId={}", saved.getId(), userId);
        return saved;
    }

    @Override
    @Transactional
    public void deleteContact(Long contactId) {
        Long userId = currentUserId();
        log.info("Deleting contact: contactId={}, userId={}", contactId, userId);

        Contact existing = getOwnedContactOrThrow(contactId, userId);

        contactRepository.delete(existing);
        log.info("Contact deleted: contactId={}, userId={}", contactId, userId);
    }

    private Contact getOwnedContactOrThrow(Long contactId, Long userId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ContactNotFoundException("Contact not found"));

        if (!userId.equals(contact.getUserId())) {
            log.error("Ownership violation: contactId={}, ownerUserId={}, currentUserId={}",
                    contactId, contact.getUserId(), userId);
            throw new OwnershipViolationException("You do not have access to this contact");
        }

        return contact;
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? String.valueOf(auth.getPrincipal()) : null;
        if (email == null || email.isBlank()) {
            log.error("Missing authentication principal while accessing contacts");
            throw new UserNotFoundException("Unauthorized");
        }

        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private String normalizeBlankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

