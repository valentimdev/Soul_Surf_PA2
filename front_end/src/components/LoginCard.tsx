import surf01 from "../../assets/login_page/surf01.jpg"
import surf02 from "../../assets/login_page/surf02.jpg"

import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
  Input,
  Label,
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from "@/components/ui/application"

function LoginCard() {
  return (
    <div className="h-screen w-full flex flex-col">
      <header className="p-6 text-xl font-semibold">
      </header>

      <ResizablePanelGroup direction="horizontal" className="flex-1">
        <ResizablePanel defaultSize={30}>
          <div className="h-full flex items-center justify-center">
              <img src={surf01} alt="Foto Surf 1" className="w-full h-full object-cover" />
          </div>
        </ResizablePanel>

        <ResizableHandle />

        <ResizablePanel defaultSize={40}>
          <div className="h-full flex items-center justify-center">
            <Card className="w-full max-w-sm border">
              <CardHeader>
                <CardTitle>Entre na sua conta</CardTitle>
                <CardDescription>
                  Insira seu email e senha para acessar sua conta.
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form>
                  <div className="flex flex-col gap-6">
                    <div className="grid gap-2">
                      <Label htmlFor="email">Email</Label>
                      <Input
                        id="email"
                        type="email"
                        placeholder="exemplo@exemplo.com"
                        required
                      />
                    </div>
                    <div className="grid gap-2">
                      <div className="flex items-center">
                        <Label htmlFor="password">Senha</Label>
                        <a
                          href="#"
                          className="ml-auto inline-block text-sm underline-offset-4 hover:underline"
                        >
                          Esqueceu a senha?
                        </a>
                      </div>
                      <Input id="password" type="password" required />
                    </div>
                  </div>
                </form>
              </CardContent>
              <CardFooter className="flex-col gap-2">
                <Button type="submit" className="w-full">
                  Login
                </Button>
                <Button variant="outline" className="w-full">
                  Login com o Google
                </Button>
              </CardFooter>
            </Card>
          </div>
        </ResizablePanel>

        <ResizableHandle />

        <ResizablePanel defaultSize={30}>
          <div className="h-full flex items-center justify-center">
              <img src={surf02} alt="Foto Surf 2" className="w-full h-full object-cover" />
          </div>
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  )
}

export default LoginCard