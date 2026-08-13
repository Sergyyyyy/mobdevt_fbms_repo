package com.example.infodbm_finals.ui.feedback.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.infodbm_finals.domain.AnswerValue
import com.example.infodbm_finals.domain.Question
import com.example.infodbm_finals.domain.QuestionType
import com.example.infodbm_finals.ui.theme.KioskColors

/**
 * Dispatches to the right widget based on question.type. This is the single
 * place that needs updating if a 6th questionType is ever added.
 *
 * @param questionNumber 1-based position in the survey, used for the
 *   "Question N" header shown in the kiosk design (the API's own `order`
 *   field is what determines this position — see FeedbackScreen.kt).
 */
@Composable
fun QuestionInput(
    questionNumber: Int,
    question: Question,
    currentAnswer: AnswerValue?,
    isMissing: Boolean,
    onAnswer: (AnswerValue) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 36.dp)) {
        Text(
            text = "Question $questionNumber",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = KioskColors.ink
        )
        Text(
            text = question.text + if (question.required) " *" else "",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = KioskColors.inkSoft,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        when (question.type) {
            QuestionType.RATING ->
                StarRatingInput(
                    selected = (currentAnswer as? AnswerValue.Rating)?.value,
                    onSelect = { onAnswer(AnswerValue.Rating(it)) }
                )

            QuestionType.YES_NO ->
                YesNoInput(
                    selected = (currentAnswer as? AnswerValue.YesNo)?.value,
                    onSelect = { onAnswer(AnswerValue.YesNo(it)) }
                )

            QuestionType.MULTIPLE_CHOICE ->
                MultipleChoiceInput(
                    options = question.options,
                    selected = (currentAnswer as? AnswerValue.Text)?.value,
                    onSelect = { onAnswer(AnswerValue.Text(it)) }
                )

            QuestionType.SHORT_TEXT ->
                TextInput(
                    value = (currentAnswer as? AnswerValue.Text)?.value ?: "",
                    singleLine = true,
                    onChange = { onAnswer(AnswerValue.Text(it)) }
                )

            QuestionType.LONG_TEXT ->
                TextInput(
                    value = (currentAnswer as? AnswerValue.Text)?.value ?: "",
                    singleLine = false,
                    onChange = { onAnswer(AnswerValue.Text(it)) }
                )
        }

        if (isMissing) {
            Text(
                text = "This question is required.",
                color = Color(0xFFB3261E),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

/** 5-star row with "very dissatisfied" / "very satisfied" labels, matching the mockup. */
@Composable
private fun StarRatingInput(selected: Int?, onSelect: (Int) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (1..5).forEach { value ->
                val filled = selected != null && value <= selected
                Text(
                    text = if (filled) "\u2605" else "\u2606", // ★ / ☆
                    fontSize = 34.sp,
                    color = KioskColors.starFilled,
                    modifier = Modifier.clickable { onSelect(value) }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("very dissatisfied", fontSize = 11.sp, color = KioskColors.inkSoft)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(1.dp)
                    .background(KioskColors.inkSoft.copy(alpha = 0.5f))
            )
            Text("very satisfied", fontSize = 11.sp, color = KioskColors.inkSoft)
        }
    }
}

@Composable
private fun YesNoInput(selected: Boolean?, onSelect: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { onSelect(true) },
            colors = kioskButtonColors(selected == true)
        ) { Text("Yes") }
        Button(
            onClick = { onSelect(false) },
            colors = kioskButtonColors(selected == false)
        ) { Text("No") }
    }
}

@Composable
private fun MultipleChoiceInput(options: List<String>, selected: String?, onSelect: (String) -> Unit) {
    Column {
        options.forEach { option ->
            Button(
                onClick = { onSelect(option) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = kioskButtonColors(selected == option)
            ) { Text(option) }
        }
    }
}

@Composable
private fun TextInput(value: String, singleLine: Boolean, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = singleLine,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = KioskColors.cardWhite,
            unfocusedContainerColor = KioskColors.cardWhite
        )
    )
}

@Composable
private fun kioskButtonColors(selected: Boolean) = if (selected) {
    ButtonDefaults.buttonColors(
        containerColor = KioskColors.ink,
        contentColor = KioskColors.cardWhite
    )
} else {
    ButtonDefaults.outlinedButtonColors(
        contentColor = KioskColors.ink
    )
}








