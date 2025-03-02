import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BillService } from 'src/app/services/bill.service';
import { CategoryService } from 'src/app/services/category.service';
import { ProductService } from 'src/app/services/product.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-manage-order',
  templateUrl: './manage-order.component.html',
  styleUrls: ['./manage-order.component.scss'],
})
export class ManageOrderComponent implements OnInit {
  displayedColumns: string[] = ['name', 'category', 'price', 'quantity', 'total', 'edit',];
  dataSource: any = [];
  manageOrderForm: any = FormGroup;
  categorys: any = [];
  products: any = [];
  price: any;
  stock: any;
  kg: any;
  totalAmount: number = 0;
  responseMessage: any;

  constructor(
    private formBulider: FormBuilder,
    private categoryService: CategoryService,
    private productService: ProductService,
    private snackbarService: SnackbarService,
    private billService: BillService,
  ) { }

  ngOnInit(): void {
    this.getCategorys();
    this.manageOrderForm = this.formBulider.group({
      name: [null,Validators.required],
      email: [null,[Validators.pattern(GlobalConstants.emailRegex)],],
      contactNumber: [null, [Validators.required, Validators.pattern(GlobalConstants.contactNumberRegex)]],
      paymentMethod: [null, [Validators.required]],
      product: [null, [Validators.required]],
      category: [null, [Validators.required]],
      quantity: [null, [Validators.required]],
      price: [null, [Validators.required]],
      total: [0, [Validators.required]],
      stock: [null],
    });
  }

  getCategorys() {
    this.categoryService.getFilteredCategorys().subscribe((response: any) => {
        this.categorys = response;
      },(error: any) => {
        console.log(error);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(this.responseMessage,GlobalConstants.error);
      })
  }

  getProductsByCategory(value:any) {
    this.productService.getProductByCategory(value.id).subscribe((response:any) => {
        this.products = response;
        this.manageOrderForm.controls['price'].setValue('');
        this.manageOrderForm.controls['quantity'].setValue('');
        this.manageOrderForm.controls['total'].setValue(0);
        this.manageOrderForm.controls['stock'].setValue('');
      },(error:any) => {
        console.log(error);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(this.responseMessage,GlobalConstants.error);
      })
  }

  getProductDetails(value:any) {
    this.productService.getById(value.id).subscribe((response:any) => {
        this.price = response.price;
        this.manageOrderForm.controls['price'].setValue(response.price);
        this.manageOrderForm.controls['quantity'].setValue('1');
        this.manageOrderForm.controls['total'].setValue(this.price * 1);
        this.manageOrderForm.controls['stock'].setValue(response.stock)
      },(error: any) => {
        console.log(error);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(this.responseMessage,GlobalConstants.error);
      })
  }

  setQuantity(value:any) {
    var temp = this.manageOrderForm.controls['quantity'].value;
    if (temp > 0) {
      this.manageOrderForm.controls['total'].setValue(this.manageOrderForm.controls['quantity'].value * this.manageOrderForm.controls['price'].value);
    } else if (temp != '') {
      this.manageOrderForm.controls['quantity'].setValue('1');
      this.manageOrderForm.controls['total'].setValue(this.manageOrderForm.controls['quantity'].value * this.manageOrderForm.controls['price'].value);
    }
  }

  validateProductAdd() {
    if (this.manageOrderForm.controls['total'].value === 0 || this.manageOrderForm.controls['total'].value === null ||
      this.manageOrderForm.controls['quantity'].value <= 0) {
      return true;
    } else {
      return false;
    }
  }

  validateSubmit() {
    var formData = this.manageOrderForm.value;
    if (this.totalAmount === 0 || this.manageOrderForm.controls['name'].value === null || this.manageOrderForm.controls['contactNumber'].value === null || this.manageOrderForm.controls['paymentMethod'].value === null){
      return true;
    } else {
      return false;
    }
  }

  add() {
    var formData = this.manageOrderForm.value;
    var productName = this.dataSource.find((e:{id:number}) => e.id === formData.product.id);
    if (productName === undefined) {
      this.totalAmount = this.totalAmount + formData.total;
      this.dataSource.push({id:formData.product.id,name:formData.product.name,category:formData.category.name,quantity:formData.quantity,price:formData.price,total:formData.total, stock: formData.product.stock, });
      this.dataSource = [...this.dataSource];
      this.snackbarService.openSnackBar(GlobalConstants.productAdded, 'Success');
    } else {
      this.snackbarService.openSnackBar(GlobalConstants.productExistError,GlobalConstants.error);
    }
  }

  handleDeleteAction(value:any, element:any) {
    this.totalAmount = this.totalAmount - element.total;
    this.dataSource.splice(value, 1);
    this.dataSource = [...this.dataSource];
    this.snackbarService.openSnackBar(GlobalConstants.productDeleted, 'Success');
  }
  
  

  submitAction() {
    const formData = this.manageOrderForm.value;
    const data = {
      name: formData.name,
      email: formData.email,
      contactNumber: formData.contactNumber,
      paymentMethod: formData.paymentMethod,
      totalAmount: this.totalAmount.toString(),
      productDetails: JSON.stringify(this.dataSource),
    };
  
    // Step 1: Check stock availability
    for (const item of this.dataSource) {
      if (item.stock < item.quantity) {
        // Insufficient stock for this item
        this.snackbarService.openSnackBar(
          `Insufficient stock for ${item.name}`,
          GlobalConstants.error
        );
        return; // Prevent order submission
      }
    }
  
    // Step 2: Generate the bill
    this.billService.generateReport(data).subscribe(
      (response: any) => {
        this.downloadFile(response?.uuid);
  
        // Step 3: Update stock for each product 
        this.dataSource.forEach((item: any) => {
          const updatedStock = item.stock - item.quantity;
  
          if (updatedStock >= 0) {
            this.productService.updateStock(item.id, updatedStock).subscribe(
              () => {
                this.snackbarService.openSnackBar(
                  `Stock successfully updated for Each product.`,
                  'Success'
                );
              },
              (error: any) => {
                console.error('Stock update failed:', error);
                this.snackbarService.openSnackBar(
                  `Failed to update stock for ${item.name}`,
                  GlobalConstants.error
                );
              }
            );
          } else {
            console.error(`Insufficient stock for ${item.name}`);
            this.snackbarService.openSnackBar(
              `Insufficient stock for ${item.name}`,
              GlobalConstants.error
            );
          }
        });
  
        // Reset the form and variables
        this.manageOrderForm.reset();
        this.dataSource = [];
        this.totalAmount = 0;
      },
      (error: any) => {
        console.log(error);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
      }
    );
  }
  
  


  


  downloadFile(fileName: string) {
    var data = {
      uuid: fileName,
    };
    this.billService.getPdf(data).subscribe((resonse: any) => {
      saveAs(resonse, fileName + '.pdf');
    })
  }
}
