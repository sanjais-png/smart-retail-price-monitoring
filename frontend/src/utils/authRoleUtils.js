/**
 * authRoleUtils.js
 *
 * Single source of truth for role-and-status to route mapping in FairPrice AI.
 *
 * WORKFLOW LANDING MATRIX:
 *   approvalStatus === 'PENDING_AUTHORITY' → /pending  (Awaiting Admin Review)
 *   ROLE_ADMIN                             → /admin    (Admin Control Center)
 *   ROLE_AUTHORITY                         → /authority (Authority Review Hub)
 *   ROLE_USER                              → /         (Consumer Dashboard)
 *
 * SECURITY NOTE:
 *   Frontend routes are for UX navigation. Backend Spring Security (@PreAuthorize)
 *   is the authoritative enforcement layer.
 */

/**
 * Returns the default landing path for a user based on approvalStatus and role.
 * Can accept a user object { role, approvalStatus } or (role, approvalStatus) strings.
 *
 * @param {object|string} userOrRole - User object or role string
 * @param {string} [approvalStatusStr] - Optional approval status if first param is role
 * @returns {string} - React Router path
 */
export const getRoleDefaultPath = (userOrRole, approvalStatusStr) => {
  let role = 'ROLE_USER';
  let approvalStatus = 'ACTIVE';

  if (typeof userOrRole === 'object' && userOrRole !== null) {
    role = userOrRole.role || 'ROLE_USER';
    approvalStatus = userOrRole.approvalStatus || 'ACTIVE';
  } else if (typeof userOrRole === 'string') {
    role = userOrRole;
    approvalStatus = approvalStatusStr || 'ACTIVE';
  }

  // 1. HIGHEST PRIORITY: Pending Authority Access Request -> /pending
  if (approvalStatus === 'PENDING_AUTHORITY') {
    return '/pending';
  }

  // 2. Admin -> /admin
  if (role === 'ROLE_ADMIN') {
    return '/admin';
  }

  // 3. Approved Authority -> /authority
  if (role === 'ROLE_AUTHORITY') {
    return '/authority';
  }

  // 4. Default Consumer -> /
  return '/';
};

/**
 * Returns a human-readable display label for a user's role and status.
 * @param {object|string} userOrRole
 * @returns {string}
 */
export const getRoleLabel = (userOrRole) => {
  if (typeof userOrRole === 'object' && userOrRole?.approvalStatus === 'PENDING_AUTHORITY') {
    return 'Pending Authority Applicant';
  }
  const role = typeof userOrRole === 'object' ? userOrRole?.role : userOrRole;
  if (role === 'ROLE_ADMIN') return 'Platform Admin';
  if (role === 'ROLE_AUTHORITY') return 'Authority Reviewer';
  return 'Consumer';
};

/**
 * Returns true if the given user can access the authority review hub.
 * Requires ROLE_AUTHORITY or ROLE_ADMIN AND NOT PENDING_AUTHORITY.
 * @param {object} user
 * @returns {boolean}
 */
export const canAccessAuthorityHub = (user) => {
  if (!user) return false;
  if (user.approvalStatus === 'PENDING_AUTHORITY') return false;
  return user.role === 'ROLE_AUTHORITY' || user.role === 'ROLE_ADMIN';
};

/**
 * Returns true if the given user can access the admin control center.
 * Only ROLE_ADMIN.
 * @param {object} user
 * @returns {boolean}
 */
export const canAccessAdminCenter = (user) => {
  if (!user) return false;
  return user.role === 'ROLE_ADMIN';
};
