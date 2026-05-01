package com.cms.contactmanagement.service;

import com.cms.contactmanagement.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactService {
    Contact createContact(Contact contact);
    Contact getContactById(Long contactId);
    Page<Contact> getAllContacts(Pageable pageable);
    Page<Contact> searchContacts(String firstName, String lastName, Pageable pageable);
    Contact updateContact(Long contactId, Contact updated);
    void deleteContact(Long contactId);
}

