/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 *
 * @author jagadeesh.t
 */
public class Stack2 {
    
    private String[] a;
        private int top, m;

        public Stack2(int max) {
            m = max;
            a = new String[m];
            top = -1;
        }

        public void push(String key) {
            a[++top] = key;
        }

        public String pop() {
            return (a[top--]);
        }

        public boolean isEmpty() {
            return (top == -1);
        }
    
}
