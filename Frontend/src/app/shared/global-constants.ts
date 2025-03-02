export class GlobalConstants {
  // Meesage
  public static genericError: string =
    'Something went wrong. pleases try again later';

  //Regex
  public static nameRegex: string = '[a-zA-Z]*';

  public static emailRegex: string =
    '[A-Za-z0-9._%-]+@[A-Za-z0-9._%-]+\\.[a-z]{2,3}';

  public static numberRegex: string = '^[0-9]+$';

  public static textRegex: string = '^[a-zA-Z ]+$';

  public static productExistError: string = 'Product already exists';

  public static productAdded: string = 'Product added successfully';

  public static productDeleted: string = 'Product Deleted successfully';

  public static productEdited: string = 'Product Updated successfully';

  public static productActivated: string = 'Product Status Changed successfully';

  public static categoryExistError: string = 'Company already exists';

  public static categoryAdded: string = 'Comapny added successfully';

  public static categoryDeleted: string = 'Company Deleted successfully';

  public static categoryEdited: string = 'Company Updated successfully';

  public static billDeleted: string = 'Bill Deleted successfully';

  public static userActivated: string = 'User Status Changed successfully';

  public static emailSend: string = 'Email Send successfully';

  public static passwordChanged: string = 'Password Changed successfully';

  public static contactNumberRegex: string = '^[e0-9]{10,10}$';

  public static unauthroized: string =
    'You are not authorized person to access this page.';
  //Variable
  public static error: string = 'Error';

  public static oldPasswordError: string = 'Error : Wrong Old Password';

  public static signupSuccess: string = 'Signed Up Successfully';

  public static loginSuccess: string = 'Login Successfully';

}
