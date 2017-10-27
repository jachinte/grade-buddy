/*
 * University of Victoria
 * Name: John Doe
 * Id: V00898765
 * Date: 2017/10/13
 * Assignment: A1
 * File name: V00898765P1.c
 */
#include <stdio.h>
#include <stdlib.h>

int main(void) {
    FILE* file = fopen("document.txt", "w");
    fprintf(file, "This is a demo file. - John\n");
    fclose(file);
	printf("This is A1 P1\n");
	return EXIT_SUCCESS;
}
