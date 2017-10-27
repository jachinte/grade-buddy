/*
 * University of Victoria
 * Name: Jane Doe
 * Id: V00812345
 * Date: 2017/10/13
 * Assignment: A1
 * File name: V00812345P1.c
 */
#include <stdio.h>
#include <stdlib.h>

int main(void) {
    FILE* file = fopen("document.txt", "w");
    fprintf(file, "This is a demo file. - Jane\n");
    fclose(file);
    printf("This is A1 P1\n");
    return EXIT_SUCCESS;
}
