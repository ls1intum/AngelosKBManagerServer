package com.ase.angelos_kb_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ase.angelos_kb_backend.dto.DocumentDataDTO;
import com.ase.angelos_kb_backend.dto.SampleQuestionDTO;
import com.ase.angelos_kb_backend.dto.WebsiteRequestDTO;
import com.ase.angelos_kb_backend.dto.angelos.AngelosAddDocumentRequest;
import com.ase.angelos_kb_backend.dto.angelos.AngelosAddSampleQuestionRequest;
import com.ase.angelos_kb_backend.dto.angelos.AngelosAddWebsiteRequest;
import com.ase.angelos_kb_backend.dto.angelos.AngelosEditDocumentRequest;
import com.ase.angelos_kb_backend.dto.angelos.AngelosEditSampleQuestionRequest;
import com.ase.angelos_kb_backend.dto.angelos.AngelosEditWebsiteRequest;
import com.ase.angelos_kb_backend.dto.angelos.AngelosRefreshContentRequest;

@Component
public class AngelosService {

    @Value("${angelos.url}")
    private String angelosUrl;

    private final RestTemplate restTemplate;

    public AngelosService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send a request to add a website resource to the Angelos RAG system.
     */
    public boolean sendWebsiteAddRequest(Long id, WebsiteRequestDTO websiteRequestDTO, String content, String type) {
        AngelosAddWebsiteRequest body = new AngelosAddWebsiteRequest();
        body.setId(id);
        body.setTitle(websiteRequestDTO.getTitle());
        body.setLink(websiteRequestDTO.getLink());
        body.setStudyProgramIds(websiteRequestDTO.getStudyProgramIds());
        body.setContent(content);
        body.setType(type);

        String endpoint = angelosUrl + "/website/add";
        return sendPostRequest(endpoint, body);
    }

    /**
     * Send a request to update an existing website's content.
     */
    public boolean sendWebsiteRefreshRequest(Long id, String content) {
        AngelosRefreshContentRequest body = new AngelosRefreshContentRequest();
        body.setContent(content);

        String endpoint = angelosUrl + "/website/" + id + "/refresh";
        return sendPostRequest(endpoint, body);
    }

    /**
     * Send a request to update a website's title and study programs.
     */
    public boolean sendWebsiteUpdateRequest(Long id, String title, List<Long> studyPrograms) {
        AngelosEditWebsiteRequest body = new AngelosEditWebsiteRequest();
        body.setTitle(title);
        body.setStudyPrograms(studyPrograms);

        String endpoint = angelosUrl + "/website/" + id + "/update";
        return sendPostRequest(endpoint, body);
    }

    

    /**
     * Send a request to delete a website from Angelos.
     */
    public boolean sendWebsiteDeleteRequest(Long id) {
        String endpoint = angelosUrl + "/website/" + id + "/delete";
        return sendDeleteRequest(endpoint);
    }

    /**
     * Send a request to add a document resource.
     */
    public boolean sendDocumentAddRequest(DocumentDataDTO doc, String content) {
        AngelosAddDocumentRequest body = new AngelosAddDocumentRequest();
        body.setId(doc.getId());
        body.setTitle(doc.getTitle());
        body.setStudyPrograms(doc.getStudyPrograms().stream().map(sp -> sp.getId()).toList());
        body.setContent(content);

        String endpoint = angelosUrl + "/document/add";
        return sendPostRequest(endpoint, body);
    }

    /**
     * Send a request to update an existing document's content.
     */
    public boolean sendDocumentRefreshRequest(Long id, String content) {
        AngelosRefreshContentRequest body = new AngelosRefreshContentRequest();
        body.setContent(content);

        String endpoint = angelosUrl + "/document/" + id + "/refresh";
        return sendPostRequest(endpoint, body);
    }

    /**
     * Send a request to edit a document resource.
     */
    public boolean sendDocumentEditRequest(DocumentDataDTO doc) {
        AngelosEditDocumentRequest body = new AngelosEditDocumentRequest();
        body.setTitle(doc.getTitle());
        body.setStudyPrograms(doc.getStudyPrograms().stream().map(sp -> sp.getId()).toList());

        String endpoint = angelosUrl + "/document/" + doc.getId() + "/edit";
        return sendPostRequest(endpoint, body);
    }

    /**
     * Send a request to delete a document resource.
     */
    public boolean sendDocumentDeleteRequest(Long id) {
        String endpoint = angelosUrl + "/document/" + id + "/delete";
        return sendDeleteRequest(endpoint);
    }

    /**
     * Send a request to add a sample question resource.
     */
    public boolean sendSampleQuestionAddRequest(SampleQuestionDTO sampleQuestion) {
        AngelosAddSampleQuestionRequest body = new AngelosAddSampleQuestionRequest();
        body.setId(sampleQuestion.getId());
        body.setTopic(sampleQuestion.getTopic());
        body.setQuestion(sampleQuestion.getQuestion());
        body.setAnswer(sampleQuestion.getAnswer());
        body.setStudyPrograms(sampleQuestion.getStudyPrograms().stream().map(sp -> sp.getId()).toList());

        String endpoint = angelosUrl + "/sample-question/add";
        return sendPostRequest(endpoint, body);
    }

    /**
     * Send a request to edit a sample question resource.
     */
    public boolean sendSampleQuestionEditRequest(SampleQuestionDTO sampleQuestion) {
        AngelosEditSampleQuestionRequest body = new AngelosEditSampleQuestionRequest();
        body.setTopic(sampleQuestion.getTopic());
        body.setQuestion(sampleQuestion.getQuestion());
        body.setAnswer(sampleQuestion.getAnswer());
        body.setStudyPrograms(sampleQuestion.getStudyPrograms().stream().map(sp -> sp.getId()).toList());

        String endpoint = angelosUrl + "/sample-question/edit";
        return sendPostRequest(endpoint, body);
    }

    /**
     * Send a request to delete a sample question resource.
     */
    public boolean sendSampleQuestionDeleteRequest(Long id) {
        String endpoint = angelosUrl + "/sample-question/" + id + "/delete";
        return sendDeleteRequest(endpoint);
    }

    /**
     * Helper method to send POST requests and return boolean based on success.
     */
    private boolean sendPostRequest(String endpoint, Object body) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(endpoint, body, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            // Log the error (not shown here for brevity)
            return false;
        }
    }


    /**
     * Helper method to send DELETE requests and return boolean based on success.
     */
    private boolean sendDeleteRequest(String endpoint) {
        try {
            restTemplate.delete(endpoint);
            // If delete doesn't throw an exception, we consider it successful
            return true;
        } catch (Exception e) {
            // Log the error (not shown here for brevity)
            return false;
        }
    }
}