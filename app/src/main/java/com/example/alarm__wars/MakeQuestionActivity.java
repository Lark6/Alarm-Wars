package com.example.alarm__wars;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MakeQuestionActivity extends AppCompatActivity {
    private List<Integer> numbersList = new ArrayList<>();
    private List<String> operatorsList = new ArrayList<>();
    private LinearLayout mainLayout;
    private int count = 2;
    private final int MAX_COUNT = 6;
    private TextView resultTextView;
    private EditText num;
    private DatabaseReference mDatabase;
    private String hostCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("rooms");
        hostCode = getIntent().getStringExtra("hostCode");

        setContentView(R.layout.activity_make_question);
        mainLayout = findViewById(R.id.mainLayout);
        Button addButton = findViewById(R.id.buttonAdd);
        Button checkButton = findViewById(R.id.check_question);
        Button submitButton = findViewById(R.id.Submit);
        resultTextView = findViewById(R.id.resultTextView);
        num = findViewById(R.id.numberEditText_1);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (count < MAX_COUNT) {
                    addOperationView();
                    count++;
                }
            }
        });

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateResult();
                calculateResult();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String question = updateResult();
                mDatabase.child(hostCode).child("question").setValue(question); // 문제와 답, 제출상태 서버로 전송
                mDatabase.child(hostCode).child("answer").setValue(evaluateExpression(question));
                mDatabase.child(hostCode).child("questionSubmitted").setValue(true);
                Intent intent = new Intent(MakeQuestionActivity.this, EndAlarmActivity.class);
                intent.putExtra("hostCode", hostCode);
                startActivity(intent);
                finish();
            }
        });
    }

    private void addOperationView() {
        LinearLayout operationLayout = new LinearLayout(this);
        operationLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        operationLayout.setOrientation(LinearLayout.VERTICAL);
        operationLayout.setGravity(Gravity.CENTER_VERTICAL);

        View itemView = getLayoutInflater().inflate(R.layout.add_op_and_num, null);

        Spinner spinner = findViewById(R.id.operatorSpinner);
        EditText editText = itemView.findViewById(R.id.numberEditText);
        ImageButton deleteButton = itemView.findViewById(R.id.deleteButton);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLayout.removeView(operationLayout);
                count--;
            }
        });
        operationLayout.addView(itemView);
        mainLayout.addView(operationLayout);
    }

    private String updateResult() {
        StringBuilder expression = new StringBuilder();
        // 첫번째 줄 숫자 읽기
        String num1 = num.getText().toString().trim();
        expression.append(num1).append(" ");
        // 두번째 줄 숫자, 연산자 읽기
        LinearLayout operationLayout = (LinearLayout) mainLayout.getChildAt(1);
        Spinner spinner = operationLayout.findViewById(R.id.operatorSpinner);
        EditText editText = operationLayout.findViewById(R.id.numberEditText);
        String operator = spinner.getSelectedItem().toString();
        String number = editText.getText().toString().trim();

        if (!number.isEmpty()) {
            expression.append(operator).append(" ").append(number).append(" ");
        }

        for (int i = 2; i < count; i++) {
            operationLayout = (LinearLayout) mainLayout.getChildAt(i);
            View itemView = operationLayout.getChildAt(0);
            spinner = itemView.findViewById(R.id.operatorSpinner);
            editText = itemView.findViewById(R.id.numberEditText);

            operator = spinner.getSelectedItem().toString();
            number = editText.getText().toString().trim();

            if (!number.isEmpty()) {
                expression.append(operator).append(" ").append(number).append(" ");
            }
        }
        resultTextView.setText(expression.toString());
        return expression.toString();
    }

    private void calculateResult() {
        // 계산 결과 표시
        try {

            String expression = resultTextView.getText().toString();
            String result = evaluateExpression(expression);
            Toast.makeText(this, "계산 결과: " + result, Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "숫자 형식에 오류가 있습니다.", Toast.LENGTH_SHORT).show();
        } catch (ArithmeticException e) {
            Toast.makeText(this, "0으로 나눌 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private String evaluateExpression(String expression) {
        Stack<Integer> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();

        // 연산자 우선순위를 저장하는 맵
        Map<Character, Integer> precedence = new HashMap<>();
        precedence.put('+', 1);
        precedence.put('-', 1);
        precedence.put('*', 2);
        precedence.put('/', 2);

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (Character.isDigit(c)) {
                // 숫자인 경우 숫자 스택에 추가
                int num = 0;
                while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
                    num = num * 10 + (expression.charAt(i) - '0');
                    i++;
                }
                numbers.push(num);
                i--; // 숫자 다음 인덱스로 이동
            } else if (c == '(') {
                // 여는 괄호인 경우 연산자 스택에 추가
                operators.push(c);
            } else if (c == ')') {
                // 닫는 괄호인 경우 해당 괄호까지의 계산 수행
                while (!operators.isEmpty() && operators.peek() != '(') {
                    calculate(numbers, operators);
                }
                operators.pop(); // 여는 괄호 제거
            } else if (isOperator(c)) {
                // 연산자인 경우
                while (!operators.isEmpty() && precedence.get(operators.peek()) >= precedence.get(c)) {
                    calculate(numbers, operators);
                }
                operators.push(c);
            }
        }

        // 나머지 연산자들 계산
        while (!operators.isEmpty()) {
            calculate(numbers, operators);
        }

        return String.valueOf(numbers.pop());
    }

    private void calculate(Stack<Integer> numbers, Stack<Character> operators) {
        int num2 = numbers.pop();
        int num1 = numbers.pop();
        char operator = operators.pop();
        int result = performOperation(num1, num2, operator);
        numbers.push(result);
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private int performOperation(int num1, int num2, char operator) {
        switch (operator) {
            case '+':
                return num1 + num2;
            case '-':
                return num1 - num2;
            case '*':
                return num1 * num2;
            case '/':
                if (num2 != 0) {
                    return num1 / num2;
                } else {
                    throw new ArithmeticException("Division by zero");
                }
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

}

