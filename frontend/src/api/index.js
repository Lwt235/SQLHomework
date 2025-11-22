import api from './axios'

export const authAPI = {
  login(data) {
    return api.post('/auth/login', data)
  },
  register(data) {
    return api.post('/auth/register', data)
  },
  deactivateAccount() {
    return api.post('/auth/deactivate')
  }
}

export const userAPI = {
  getAllUsers() {
    return api.get('/users')
  },
  getInactiveUsers() {
    return api.get('/users/inactive')
  },
  getUserById(id) {
    return api.get(`/users/${id}`)
  },
  updateUserProfile(id, data) {
    return api.put(`/users/${id}/profile`, data)
  },
  activateUser(id) {
    return api.put(`/users/${id}/activate`)
  },
  suspendUser(id) {
    return api.put(`/users/${id}/suspend`)
  },
  updateUserStatus(id, status) {
    return api.put(`/users/${id}/status`, null, { params: { status } })
  },
  deleteUser(id) {
    return api.delete(`/users/${id}`)
  },
  getAllRoles() {
    return api.get('/users/roles')
  },
  getUserRoles(id) {
    return api.get(`/users/${id}/roles`)
  },
  assignRoles(id, roleIds) {
    return api.put(`/users/${id}/roles`, { roleIds })
  }
}

export const competitionAPI = {
  getAllCompetitions() {
    return api.get('/competitions/list')
  },
  getCompetitionById(id) {
    return api.get(`/competitions/${id}`)
  },
  createCompetition(data) {
    return api.post('/competitions', data)
  },
  updateCompetition(id, data) {
    return api.put(`/competitions/${id}`, data)
  },
  deleteCompetition(id) {
    return api.delete(`/competitions/${id}`)
  },
  getCompetitionsByStatus(status) {
    return api.get(`/competitions/status/${status}`)
  }
}

export const registrationAPI = {
  getAllRegistrations() {
    return api.get('/registrations')
  },
  getRegistrationById(id) {
    return api.get(`/registrations/${id}`)
  },
  createRegistration(data) {
    return api.post('/registrations', data)
  },
  updateRegistrationStatus(id, status, auditUserId) {
    return api.put(`/registrations/${id}/status`, null, { 
      params: { status, auditUserId } 
    })
  },
  getRegistrationsByCompetition(competitionId) {
    return api.get(`/registrations/competition/${competitionId}`)
  },
  getRegistrationsByUser(userId) {
    return api.get(`/registrations/user/${userId}`)
  }
}

export const submissionAPI = {
  getAllSubmissions() {
    return api.get('/submissions')
  },
  getSubmissionById(id) {
    return api.get(`/submissions/${id}`)
  },
  createSubmission(data) {
    return api.post('/submissions', data)
  },
  updateSubmission(id, data) {
    return api.put(`/submissions/${id}`, data)
  },
  submitSubmission(id) {
    return api.post(`/submissions/${id}/submit`)
  },
  lockSubmission(id) {
    return api.post(`/submissions/${id}/lock`)
  },
  getSubmissionsByRegistration(registrationId) {
    return api.get(`/submissions/registration/${registrationId}`)
  }
}

export const judgeAPI = {
  getAllAssignments() {
    return api.get('/judge/assignments')
  },
  createAssignment(data) {
    return api.post('/judge/assignments', data)
  },
  updateAssignment(data) {
    return api.put('/judge/assignments', data)
  },
  getAssignmentsByJudge(userId) {
    return api.get(`/judge/assignments/judge/${userId}`)
  },
  getAssignmentsBySubmission(submissionId) {
    return api.get(`/judge/assignments/submission/${submissionId}`)
  },
  manualAssignJudge(data) {
    return api.post('/judge/assignments/manual', data)
  },
  randomAssignJudges(judgesPerSubmission) {
    return api.post('/judge/assignments/random', { judgesPerSubmission })
  },
  confirmReview(userId, submissionId) {
    return api.post('/judge/assignments/confirm', { userId, submissionId })
  }
}

export const awardAPI = {
  getAllAwards() {
    return api.get('/awards')
  },
  getAwardById(id) {
    return api.get(`/awards/${id}`)
  },
  createAward(data) {
    return api.post('/awards', data)
  },
  updateAward(id, data) {
    return api.put(`/awards/${id}`, data)
  },
  getAwardsByCompetition(competitionId) {
    return api.get(`/awards/competition/${competitionId}`)
  },
  grantAward(registrationId, awardId, certificateNo) {
    return api.post('/awards/grant', null, { 
      params: { registrationId, awardId, certificateNo } 
    })
  },
  getAwardResultsByRegistration(registrationId) {
    return api.get(`/awards/results/registration/${registrationId}`)
  },
  getAwardResultsByAward(awardId) {
    return api.get(`/awards/results/award/${awardId}`)
  }
}

export const teamAPI = {
  getAllTeams() {
    return api.get('/teams')
  },
  getTeamById(id) {
    return api.get(`/teams/${id}`)
  },
  getTeamMembers(id) {
    return api.get(`/teams/${id}/members`)
  },
  getTeamMembersWithDetails(id) {
    return api.get(`/teams/${id}/members/details`)
  },
  getTeamsByUser(userId) {
    return api.get(`/teams/user/${userId}`)
  },
  getMyCaptainTeams() {
    return api.get('/teams/my-captain-teams')
  },
  createTeam(data) {
    return api.post('/teams', data)
  },
  updateTeam(id, data) {
    return api.put(`/teams/${id}`, data)
  },
  deleteTeam(id) {
    return api.delete(`/teams/${id}`)
  },
  addTeamMember(teamId, data) {
    return api.post(`/teams/${teamId}/members`, data)
  },
  removeTeamMember(teamId, userId) {
    return api.delete(`/teams/${teamId}/members/${userId}`)
  },
  transferCaptain(teamId, userId) {
    return api.post(`/teams/${teamId}/transfer-captain`, { userId })
  },
  updateMemberRole(teamId, userId, role) {
    return api.post(`/teams/${teamId}/update-member-role`, { userId, role })
  },
  searchUsers(query) {
    return api.get('/teams/search-users', { params: { query } })
  }
}

export const notificationAPI = {
  getUserNotifications(userId) {
    return api.get(`/notifications/user/${userId}`)
  },
  getUnreadNotifications(userId) {
    return api.get(`/notifications/user/${userId}/unread`)
  },
  getUnreadCount(userId) {
    return api.get(`/notifications/user/${userId}/unread-count`)
  },
  markAsRead(notificationId) {
    return api.post(`/notifications/${notificationId}/read`)
  },
  markAllAsRead(userId) {
    return api.post(`/notifications/user/${userId}/read-all`)
  },
  deleteNotification(notificationId) {
    return api.delete(`/notifications/${notificationId}`)
  },
  batchDeleteNotifications(notificationIds) {
    return api.delete('/notifications/batch', { data: { notificationIds } })
  }
}
