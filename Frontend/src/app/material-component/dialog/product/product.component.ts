import { Component, Inject, OnInit, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CategoryService } from 'src/app/services/category.service';
import { ProductService } from 'src/app/services/product.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { GlobalConstants } from 'src/app/shared/global-constants';

@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.scss'],
})
export class ProductComponent implements OnInit {
  onAddProduct = new EventEmitter();
  onEditProduct = new EventEmitter();
  productForm: any = FormGroup;
  dialogAction: any = 'Add';
  action: any = 'Add';
  responseMessage: any;
  categorys: any = [];

  constructor(
    @Inject(MAT_DIALOG_DATA) public dialogData: any,
    private formBulider: FormBuilder,
    protected productService: ProductService,
    public dialogRef: MatDialogRef<ProductComponent>,
    private snackbarService: SnackbarService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.productForm = this.formBulider.group({
      name: [null,Validators.required],
      price: [null, [Validators.required, Validators.pattern(GlobalConstants.numberRegex)]],
      categoryId: [null, Validators.required],
      stock: [null, [Validators.required, Validators.pattern(GlobalConstants.numberRegex)]],
      description: [null, [Validators.required, Validators.pattern(GlobalConstants.textRegex)]],
    });
    if (this.dialogData.action === 'Edit') {
      this.dialogAction = 'Edit';
      this.action = 'Update';
      this.productForm.patchValue(this.dialogData.data);
    }
    this.getCategorys();
  }

  getCategorys() {
    this.categoryService.getCategorys().subscribe(
      (response: any) => {
        this.categorys = response;
      },
      (error) => {
        console.error(error);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(
          this.responseMessage,
          GlobalConstants.error
        );
      }
    );
  }

  handleSubmit() {
    if (this.dialogAction === 'Edit') {
      this.edit();
    } else {
      this.add();
    }
  }

  
  add() {
    var formData = this.productForm.value;
    var data = {
      name: formData.name,
      price: formData.price,
      categoryId: formData.categoryId,
      stock: formData.stock,
      description: formData.description,
    };
  
    // Fetch existing products and check for duplicates
    this.productService.getProducts().subscribe((existingProducts: any) => {
      // Check for an existing product with the exact name and category
      const existingProduct = existingProducts.find(
        (product: any) => 
          product.name === data.name && 
          product.categoryId === data.categoryId
      );
    
      if (existingProduct) {
        this.responseMessage = 'Product already exists.';
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
      } else {
        // If no exact match, check for an existing product with the name in uppercase
        const upperCaseProduct = existingProducts.find(
          (product: any) => 
            product.name.toUpperCase() === data.name.toUpperCase() && 
            product.categoryId === data.categoryId
        );
    
        if (upperCaseProduct) {
          this.responseMessage = 'Product already exists.';
          this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
        } else {
          // If no match found, proceed to add the new product
          this.productService.add(data).subscribe(
            (response: any) => {
              this.dialogRef.close();
              this.onAddProduct.emit();
              this.responseMessage = response.message;
              this.snackbarService.openSnackBar(GlobalConstants.productAdded, 'Success');
            },
            (error) => {
              this.dialogRef.close();
              console.error(error);
              if (error.error?.message) {
                this.responseMessage = error.error?.message;
              } else {
                this.responseMessage = GlobalConstants.genericError;
              }
              alert(this.responseMessage + ' ' + GlobalConstants.error);
              this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
            }
          );
        }
      }
    });
  }    

  
  edit() {
    var formData = this.productForm.value;
    var data = {
      id: this.dialogData.data.id,
      name: formData.name,
      price: formData.price,
      categoryId: formData.categoryId,
      stock: formData.stock,
      description: formData.description,
    };
    this.productService.update(data).subscribe(
      (response: any) => {
        this.dialogRef.close();
        this.onEditProduct.emit();
        this.responseMessage = response.message;
        this.snackbarService.openSnackBar(GlobalConstants.productEdited, 'Success');
      },
      (error) => {
        this.dialogRef.close();
        console.error(error);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        alert(this.responseMessage + ' ' + GlobalConstants.error);
        this.snackbarService.openSnackBar(
          this.responseMessage,
          GlobalConstants.error
        );
      }
    );
  }
}
