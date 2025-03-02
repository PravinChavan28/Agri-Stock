import { Component, Inject, EventEmitter, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { CategoryService } from 'src/app/services/category.service';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { GlobalConstants } from 'src/app/shared/global-constants';
import { SnackbarService } from 'src/app/services/snackbar.service';

@Component({
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.scss'],
})
export class CategoryComponent implements OnInit {
  onAddCategory = new EventEmitter();
  onEditCategory = new EventEmitter();
  categoryForm: any = FormGroup;
  dialogAction: any = 'Add';
  action: any = 'Add';

  responseMessage: any;

  constructor(
    @Inject(MAT_DIALOG_DATA) public dialogData: any,
    private formBulider: FormBuilder,
    protected categoryService: CategoryService,
    public dialogRef: MatDialogRef<CategoryComponent>,
    private snackbarService: SnackbarService
  ) {}

  ngOnInit(): void {
    this.categoryForm = this.formBulider.group({
      name: [null,[Validators.required]],
    });
    if (this.dialogData.action === 'Edit') {
      this.dialogAction = 'Edit';
      this.action = 'Update';
      this.categoryForm.patchValue(this.dialogData.data);
    }
  }

  handleSubmit() {
    if (this.dialogAction === 'Edit') {
      this.edit();
    } else {
      this.add();
    }
  }


  add() {
    var formData = this.categoryForm.value;
    var data = {
      name: formData.name,
    };
  
    // Fetch existing categories and check for duplicates
    this.categoryService.getCategorys().subscribe((existingCategories: any) => {
      // Check for an existing category with the exact name
      const existingCategory = existingCategories.find(
        (category: any) => category.name === data.name
      );
  
      if (existingCategory) {
        this.responseMessage = 'Category already exists.';
        this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
      } else {
        // If no exact match, check for an existing category with the name in uppercase
        const upperCaseCategory = existingCategories.find(
          (category: any) =>
            category.name.toUpperCase() === data.name.toUpperCase()
        );
  
        if (upperCaseCategory) {
          this.responseMessage = 'Category already exists.';
          this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
        } else {
          // If no match found, proceed to add the new category
          this.categoryService.add(data).subscribe(
            (response: any) => {
              this.dialogRef.close();
              this.onAddCategory.emit();
              this.responseMessage = response.message;
              this.snackbarService.openSnackBar(GlobalConstants.categoryAdded, 'Success');
            },
            (error) => {
              this.dialogRef.close();
              console.error(error);
              if (error.error?.message) {
                this.responseMessage = error.error?.message;
              } else {
                this.responseMessage = GlobalConstants.genericError;
              }
              this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
            }
          );
        }
      }
    });
  }
  



  edit() {
    var formData = this.categoryForm.value;
    var data = {
      id: this.dialogData.data.id,
      name: formData.name
    };
    this.categoryService.update(data).subscribe((response: any) => {
        this.dialogRef.close();
        this.onEditCategory.emit();
        this.responseMessage = response.message;
        this.snackbarService.openSnackBar(GlobalConstants.categoryEdited, 'Success');
      },(error) => {
        this.dialogRef.close();
        console.error(error);
        if (error.error?.message) {
          this.responseMessage = error.error?.message;
        } else {
          this.responseMessage = GlobalConstants.genericError;
        }
        this.snackbarService.openSnackBar(this.responseMessage,GlobalConstants.error);
      }
    )
  }
}
