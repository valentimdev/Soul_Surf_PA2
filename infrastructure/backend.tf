resource "aws_instance" "backend" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.micro"
  subnet_id              = module.vpc.public_subnets[0]
  vpc_security_group_ids = [module.vpc.default_security_group_id]

  tags = {
    Name = "${var.project_name}-backend"
  }

  user_data = file("${path.module}/user_data.sh")
}
