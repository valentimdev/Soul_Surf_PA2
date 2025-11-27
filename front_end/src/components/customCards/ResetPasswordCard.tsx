// src/components/customCards/ResetPasswordCard.tsx

import { useState, useEffect } from "react"
import { useSearchParams, Link, useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Loader2, ArrowLeft, CheckCircle } from "lucide-react"
import axios from "axios"

export default function ResetPasswordCard() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get("token")
  const navigate = useNavigate()

  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [isSuccess, setIsSuccess] = useState(false)

  useEffect(() => {
    if (!token) {
      alert("Link inválido ou token ausente.\n\nVocê será redirecionado.")
      navigate("/forgot-password")
    }
  }, [token, navigate])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (password !== confirmPassword) {
      alert("As senhas não coincidem!")
      return
    }

    if (password.length < 6) {
      alert("A senha deve ter no mínimo 6 caracteres.")
      return
    }

    setIsLoading(true)

    try {
      await axios.post(
        "https://soulsurfpa2-production.up.railway.app/api/auth/reset-password",
        {
          token,
          newPassword: password,
        }
      )

      setIsSuccess(true)
      alert("Senha alterada com sucesso!\n\nVocê será redirecionado para o login em 3 segundos.")

      setTimeout(() => navigate("/login"), 3000)
    } catch (error: any) {
      const msg =
        error.response?.data?.message ||
        "Token inválido, expirado ou erro no servidor."
      alert("Erro: " + msg)
    } finally {
      setIsLoading(false)
    }
  }

  // Tela de sucesso
  if (isSuccess) {
    return (
      <Card className="w-full max-w-md mx-auto border-0 shadow-none bg-transparent">
        <CardContent className="flex flex-col items-center justify-center py-16 space-y-6">
          <CheckCircle className="h-20 w-20 text-green-500" />
          <div className="text-center space-y-2">
            <p className="text-2xl font-bold">Senha alterada!</p>
            <p className="text-muted-foreground">
              Redirecionando para o login...
            </p>
          </div>
        </CardContent>
      </Card>
    )
  }

  // Formulário normal
  return (
    <Card className="w-full max-w-md mx-auto border-0 shadow-none bg-transparent">
      <CardHeader className="space-y-1 text-center">
        <CardTitle className="text-2xl font-bold">Criar nova senha</CardTitle>
        <CardDescription>
          Digite e confirme sua nova senha abaixo
        </CardDescription>
      </CardHeader>

      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            type="password"
            placeholder="Nova senha (mín. 6 caracteres)"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={isLoading}
          />
          <Input
            type="password"
            placeholder="Confirmar nova senha"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            disabled={isLoading}
          />

          <Button
            className="w-full"
            type="submit"
            disabled={isLoading || !password || !confirmPassword}
          >
            {isLoading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Alterando senha...
              </>
            ) : (
              "Alterar senha"
            )}
          </Button>
        </form>

        <div className="mt-8 text-center">
          <Link
            to="/login"
            className="text-sm text-muted-foreground hover:text-primary inline-flex items-center gap-1"
          >
            <ArrowLeft className="h-4 w-4" />
            Voltar para login
          </Link>
        </div>
      </CardContent>
    </Card>
  )
}