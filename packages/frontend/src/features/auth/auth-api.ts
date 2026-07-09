import { apiClient, mutationHeaders, withBasePath } from "@/lib/api-client";

export interface AuthUser {
  id: string;
  name: string;
  email: string;
  departmentId: string;
  departmentName: string;
  role: "ADMIN" | "EMPLOYEE";
  isManager: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export function login(request: LoginRequest): Promise<AuthUser> {
  return apiClient.post<AuthUser>("/api/auth/login", request);
}

export async function logout(): Promise<void> {
  try {
    await fetch(withBasePath("/api/auth/logout"), {
      method: "POST",
      credentials: "include",
      headers: mutationHeaders(),
      redirect: "manual",
    });
  } catch {
    // SageMaker Gateway の OIDC リダイレクトで CORS エラーになり得るが、
    // ログアウトの意図は達成されるため握りつぶす
  }
}

export function fetchCurrentUser(): Promise<AuthUser> {
  return apiClient.get<AuthUser>("/api/auth/me");
}
