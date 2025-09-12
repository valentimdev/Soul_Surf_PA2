import soulSurfIcon from "@/assets/header/SoulSurfIcon.png"
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
} from "@/components/ui/application"

function LandingCard() {
  return (
   <div className="h-full flex items-center justify-center">
            <Card className="w-full max-w-sm border">
              <CardHeader>
                <img
                    src={soulSurfIcon}
                    alt="Soul Surf Logo"
                    className="h-full w-auto"
                />  
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
                  Entrar
                </Button>
            
              </CardFooter>
            </Card>
          </div>
          
  )
}

export default LandingCard