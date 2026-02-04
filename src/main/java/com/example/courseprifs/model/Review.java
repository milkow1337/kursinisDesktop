package com.example.courseprifs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int rating;
    private String reviewText;
    private LocalDate dateCreated;
    @ManyToOne
    private BasicUser commentOwner;
    @ManyToOne
    private BasicUser feedbackUser;
    @ManyToOne
    private Chat chat;

    public Review(String reviewText, BasicUser commentOwner, Chat chat) {
        this.reviewText = reviewText;
        this.commentOwner = commentOwner;
        this.chat = chat;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = (dateCreated != null) ? dateCreated.format(formatter) : "N/A";
        String reviewer = (commentOwner != null) ? commentOwner.getLogin() : "N/A";
        String reviewed = (feedbackUser != null) ? feedbackUser.getLogin() : "N/A";
        
        return String.format("Date: %s | Rating: %d/5\nFrom: %s | To: %s\nReview: %s",
                date, rating, reviewer, reviewed, reviewText);
    }
}
