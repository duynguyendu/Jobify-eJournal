import { BrowserRouter, Routes, Route } from "react-router-dom";
import {
  Signup,
  Landing,
  Error,
  ProtectedRoute,
  Login,
  ManagerProtectedRoute,
} from "./pages";
import {
  SharedLayout,
  AuthorPaper,
  AddPaper,
  MemberSearch,
  AddReview,
  ReviewerInvitation,
  PaperDetail,
  AllReviewReport,
  ReviewReportDetail,
  JournalPaper,
  SendInvitation,
  MemberJournalDetail,
  MemberIssues,
  MemberPublishes,
  MemberIssueDetail,
  MemberPublishDetail,
} from "./pages/dashboard";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <SharedLayout viewType="member" />
            </ProtectedRoute>
          }
        >
          <Route index element={<MemberSearch />} />
          <Route path="journal/:journalId" element={<MemberJournalDetail />} />
          <Route path="journal/:journalId/issue" element={<MemberIssues />} />
          <Route
            path="journal/:journalId/publish"
            element={<MemberPublishes />}
          />
          <Route path="issue/:issueId" element={<MemberIssueDetail />} />
          <Route path="publish/:publishId" element={<MemberPublishDetail />} />
        </Route>

        <Route
          path="/author"
          element={
            <ProtectedRoute>
              <SharedLayout viewType="author" />
            </ProtectedRoute>
          }
        >
          <Route index element={<AuthorPaper />} />
          <Route path="paper-detail/:paperId" element={<PaperDetail />} />
          <Route path="submit-paper" element={<AddPaper />} />
        </Route>

        <Route
          path="/reviewer"
          element={
            <ProtectedRoute>
              <SharedLayout viewType="reviewer" />
            </ProtectedRoute>
          }
        >
          <Route index element={<AllReviewReport />} />
          <Route path="submit-review" element={<AddReview />} />
          <Route
            path="review-detail/:reviewId"
            element={<ReviewReportDetail />}
          />
          <Route path="invitation" element={<ReviewerInvitation />} />
        </Route>

        <Route
          path="/manager"
          element={
            <ManagerProtectedRoute>
              <SharedLayout viewType="manager" />
            </ManagerProtectedRoute>
          }
        >
          <Route index element={<JournalPaper />} />
          <Route path="send-invitation/:paperId" element={<SendInvitation />} />
          {/* <Route path="invite" element={<ReviewReportDetail />} /> */}
        </Route>

        <Route path="/signup" element={<Signup />} />
        <Route path="/login" element={<Login />} />
        <Route path="/landing" element={<Landing />} />
        <Route path="*" element={<Error />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
