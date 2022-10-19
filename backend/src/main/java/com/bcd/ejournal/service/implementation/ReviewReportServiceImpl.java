package com.bcd.ejournal.service.implementation;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bcd.ejournal.domain.dto.request.ReviewReportSearchFilterRequest;
import com.bcd.ejournal.domain.dto.request.ReviewReportSubmitRequest;
import com.bcd.ejournal.domain.dto.response.PagingResponse;
import com.bcd.ejournal.domain.dto.response.ReviewReportDetailResponse;
import com.bcd.ejournal.domain.entity.Paper;
import com.bcd.ejournal.domain.entity.ReviewReport;
import com.bcd.ejournal.domain.entity.Reviewer;
import com.bcd.ejournal.domain.enums.PaperStatus;
import com.bcd.ejournal.domain.enums.ReviewReportStatus;
import com.bcd.ejournal.domain.enums.ReviewReportVerdict;
import com.bcd.ejournal.domain.exception.ForbiddenException;
import com.bcd.ejournal.domain.exception.MethodNotAllowedException;
import com.bcd.ejournal.repository.PaperRepository;
import com.bcd.ejournal.repository.ReviewReportRepository;
import com.bcd.ejournal.repository.ReviewerRepository;
import com.bcd.ejournal.service.ReviewReportService;
import com.bcd.ejournal.utils.DTOMapper;

@Service
public class ReviewReportServiceImpl implements ReviewReportService {

    private final ReviewReportRepository reviewreportRepository;
    private final ReviewerRepository reviewerRepository;
    private final PaperRepository paperRepository;
    private final ModelMapper modelMapper;
    private final DTOMapper dtoMapper;
    @Value("${paper.file.dir}")
    private String uploadDir;

    @Autowired
    public ReviewReportServiceImpl(ReviewReportRepository reviewreportRepository, ReviewerRepository reviewerRepository, PaperRepository paperRepository, ModelMapper modelMapper, DTOMapper dtoMapper) {
        this.reviewreportRepository = reviewreportRepository;
        this.reviewerRepository = reviewerRepository;
        this.paperRepository = paperRepository;
        this.modelMapper = modelMapper;
        this.dtoMapper = dtoMapper;
    }

    @Override
    @Transactional
    public void updateReviewReport(Integer accountId, Integer reviewReportId, ReviewReportSubmitRequest req) {
        ReviewReport reviewReport = reviewreportRepository.findById(reviewReportId)
                .orElseThrow(() -> new NullPointerException("Review report not found. Id: " + reviewReportId));
        // check if reviewer ownership
        if (reviewReport.getReviewer().getReviewerId() != accountId) {
            throw new ForbiddenException("Access denied for review report. Id: " + reviewReportId);
        } 
        // check if in reviewing process
        if (reviewReport.getPaper().getStatus() != PaperStatus.REVIEWING && reviewReport.getPaper().getStatus() != PaperStatus.PENDING) {
            throw new MethodNotAllowedException("Paper not in reviewing process. Review report Id: " + reviewReportId);
        }

        modelMapper.map(req, reviewReport);
        reviewReport.setReviewDate(new Timestamp(System.currentTimeMillis()));
        reviewReport.setStatus(ReviewReportStatus.DONE);
        reviewreportRepository.save(reviewReport);
        Paper paper = reviewReport.getPaper();

        // TODO: test this
        // evaluation process
        List<ReviewReport> reviewReports = reviewreportRepository.findByPaperIdAndStatus(paper.getPaperId(), ReviewReportStatus.DONE);

        if (reviewReports.size() == 3) {
            int accepted = 0;
            int grade = 0;
            for (ReviewReport report : reviewReports) {
                grade += report.getGrade();
                if (report.getVerdict() == ReviewReportVerdict.ACCEPTED) {
                    accepted++;
                }
            }

            // accept if two or more reviewer accept 
            if (accepted >= 2) {
                paper.setStatus(PaperStatus.ACCEPTED);
            } else {
                paper.setStatus(PaperStatus.REJECTED);
            }
            // grade is avarage of total grade
            paper.setGrade(grade / 3);

            paperRepository.save(paper);
        }
    }

    @Override
    public List<ReviewReportDetailResponse> getAllReviewReport(Integer reviewerId) {
        Reviewer reviewer = reviewerRepository.findById(reviewerId)
                .orElseThrow(() -> new NullPointerException("No reviewer found. Id: " + reviewerId));
        return reviewer.getReviewReports().stream()
                .map(dtoMapper::toReviewReportDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewReportDetailResponse getReviewReport(Integer reviewerId, Integer reviewReportId) {
        ReviewReport reviewReport = reviewreportRepository.findById(reviewReportId)
            .orElseThrow(() -> new NullPointerException("No review report found. Id: " + reviewReportId));

        if (reviewReport.getReviewer().getReviewerId() != reviewerId) {
            throw new MethodNotAllowedException("Review not allow to see this");
        }

        return dtoMapper.toReviewReportDetailResponse(reviewReport);
    }

	@Override
	public PagingResponse search(ReviewReportSearchFilterRequest req) {
		int pageNum = req.getPage() != null ? req.getPage() - 1 : 0;
		Pageable page = PageRequest.of(pageNum, 10);
		Page<ReviewReport> reviewReports = reviewreportRepository.search(req, page);
        PagingResponse response = new PagingResponse();
        response.setResult(reviewReports.stream().map(dtoMapper::toReviewReportResponse)
				.collect(Collectors.toList()));
        response.setNumOfPage(reviewReports.getTotalPages());
        response.setTotalFound(reviewReports.getTotalElements());
        return response;
	}
}
