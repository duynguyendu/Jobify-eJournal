package com.bcd.ejournal.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reviewer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ReviewerId;
    private boolean invitable;

    @ManyToMany
    @JoinTable(name = "ReviewerField", joinColumns = @JoinColumn(name = "ReviewerId", referencedColumnName = "ReviewerId"), inverseJoinColumns = @JoinColumn(name = "FieldId", referencedColumnName = "FieldId"))
    private List<Field> fields;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "ReviewerId")
    private Account account;

    @OneToMany(mappedBy = "reviewer")
    private List<Invitation> invitations;

    @OneToMany(mappedBy = "reviewer")
    private List<ReviewReport> reviewReports;
}
