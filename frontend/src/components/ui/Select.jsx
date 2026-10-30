import { useId } from "react";
import { Field, FIELD } from "./Input";

export default function Select({ label, error, hint, className = "", children, ...props }) {
  const id = useId();
  return (
    <Field label={label} error={error} hint={hint} htmlFor={id}>
      <select id={id} className={`${FIELD} ${error ? "border-red-400" : "border-slate-300"} ${className}`} {...props}>
        {children}
      </select>
    </Field>
  );
}
