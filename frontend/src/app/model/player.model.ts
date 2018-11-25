export class Player {

  id: number;
  login: string;
  email: string;
  firstName: string;
  lastName: string;
  name?: string;
  image: string;

  constructor(id: number, login: string, email: string, firstName: string, lastName: string, image: string) {
    this.id = id;
    this.login = login;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.image = image;
  }

}
